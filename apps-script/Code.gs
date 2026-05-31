/**
 * D&D Helper — Google Apps Script Backend (Per-Character Sheets)
 *
 * Each character lives in its own sheet (tab) named after the character ID.
 * Character info is in rows 1-2, items in rows 4+.
 *
 * Deploy as a Web App (Deploy → New deployment → Web app) with:
 *   - Execute as: Me
 *   - Who has access: Anyone
 *
 * SPREADSHEET_ID:
 *   - Leave empty ("") to use the spreadsheet this script is bound to.
 *   - Paste a Spreadsheet ID to target a specific sheet.
 */

const SPREADSHEET_ID = ""; // <-- Paste your Spreadsheet ID here, or leave empty
const METADATA_SHEET_NAME = "Metadata";

const CHARACTER_HEADERS = [
  "ID", "Name", "PlayerName", "Race", "Class", "Level",
  "Description", "ImageUrl", "MaxHP", "CurrentHP",
  "Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma",
  "Subclass", "Background", "ExperiencePoints",
  "AppearanceJSON", "CombatJSON", "ProficienciesJSON", "WeaponsJSON", "FeaturesJSON",
  "SkillsJSON"
];

const ITEM_HEADERS = [
  "ItemID", "ItemName", "Slot", "Rarity", "StatsJSON", "Description", "Equipped", "ImageUrl"
];

/** Actions that modify data and require a lock. */
const WRITE_ACTIONS = ["saveCharacter", "deleteCharacter"];

/** Actions that only read data — no lock needed. */
const READ_ACTIONS = ["getCharacters", "getCharacter", "getLastModified"];

function doGet(e) {
  console.log("=== doGet START ===");
  console.log("Query parameters:", JSON.stringify(e.parameter));

  var requestJson = e.parameter.request;
  if (!requestJson) {
    console.error("Missing 'request' query parameter");
    return jsonOutput({ success: false, error: "Missing 'request' query parameter" });
  }

  var request = JSON.parse(requestJson);
  console.log("Parsed request:", JSON.stringify(request));
  var action = request.action;

  try {
    var result;

    if (READ_ACTIONS.indexOf(action) >= 0) {
      result = handleRequest(request);
    } else if (WRITE_ACTIONS.indexOf(action) >= 0) {
      var lock = LockService.getScriptLock();
      lock.waitLock(30000);
      try {
        result = handleRequest(request);
        SpreadsheetApp.flush();
      } finally {
        lock.releaseLock();
      }
    } else {
      result = { success: false, error: "Unknown action: " + action };
    }

    console.log("Result:", JSON.stringify(result).substring(0, 500));
    console.log("=== doGet END ===");
    return jsonOutput(result, action);
  } catch (error) {
    console.error("CRITICAL ERROR in doGet:", error);
    console.error("Stack:", error.stack);
    return jsonOutput({ success: false, error: error.toString(), stack: error.stack });
  }
}

function doPost(e) {
  console.log("=== doPost START ===");
  var lock = LockService.getScriptLock();
  lock.waitLock(30000);
  try {
    var request = JSON.parse(e.postData.contents);
    console.log("Parsed request:", JSON.stringify(request));
    var result = handleRequest(request);
    SpreadsheetApp.flush();
    console.log("=== doPost END ===");
    return jsonOutput(result, request.action);
  } catch (error) {
    console.error("CRITICAL ERROR in doPost:", error);
    console.error("Stack:", error.stack);
    return jsonOutput({ success: false, error: error.toString(), stack: error.stack });
  } finally {
    lock.releaseLock();
  }
}

function handleRequest(request) {
  var action = request.action;
  console.log("Action:", action);

  switch (action) {
    case "getCharacters":
      console.log("Routing to handleGetCharacters");
      return handleGetCharacters();
    case "getCharacter":
      console.log("Routing to handleGetCharacter, id:", request.id);
      return handleGetCharacter(request.id);
    case "saveCharacter":
      console.log("Routing to handleSaveCharacter");
      return handleSaveCharacter(request.character);
    case "deleteCharacter":
      console.log("Routing to handleDeleteCharacter, id:", request.id);
      return handleDeleteCharacter(request.id);
    case "getLastModified":
      console.log("Routing to handleGetLastModified");
      return handleGetLastModified();
    default:
      console.warn("Unknown action received:", action);
      return { success: false, error: "Unknown action: " + action };
  }
}

function jsonOutput(data, action) {
  // Note: ContentService.createTextOutput() returns a TextOutput object.
  // TextOutput does NOT support custom HTTP headers (setHeaders is not available).
  // The only configurable property is the MIME type.
  return ContentService
    .createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}

function getSpreadsheet() {
  if (SPREADSHEET_ID && SPREADSHEET_ID.trim() !== "") {
    console.log("Opening spreadsheet by ID:", SPREADSHEET_ID);
    return SpreadsheetApp.openById(SPREADSHEET_ID);
  }
  console.log("Using active (bound) spreadsheet");
  return SpreadsheetApp.getActiveSpreadsheet();
}

function getMetadataSheet() {
  var spreadsheet = getSpreadsheet();
  var sheet = spreadsheet.getSheetByName(METADATA_SHEET_NAME);
  if (!sheet) {
    console.log("Creating Metadata sheet");
    sheet = spreadsheet.insertSheet(METADATA_SHEET_NAME);
    sheet.getRange("A1").setValue("lastModified");
    sheet.getRange("B1").setValue(new Date().toISOString());
  }
  // Ensure B1 has a value even if the sheet existed but was empty
  var b1Value = sheet.getRange("B1").getValue();
  if (!b1Value) {
    console.log("Metadata B1 was empty, initializing timestamp");
    sheet.getRange("B1").setValue(new Date().toISOString());
  }
  return sheet;
}

function updateLastModified() {
  var sheet = getMetadataSheet();
  var newTimestamp = new Date().toISOString();
  sheet.getRange("B1").setValue(newTimestamp);
  console.log("Metadata timestamp updated to:", newTimestamp);
}

function handleGetLastModified() {
  var sheet = getMetadataSheet();
  var timestamp = sheet.getRange("B1").getValue();
  var result = timestamp ? timestamp.toString() : new Date().toISOString();
  console.log("getLastModified returning:", result);
  return { success: true, data: result };
}

/**
 * Simple trigger that fires when any cell is edited directly in Google Sheets.
 * This ensures the lastModified timestamp updates even when users edit sheets
 * manually (not through the app), so auto-refresh polling detects changes.
 *
 * Note: This does NOT fire when the Web App writes data via setValues().
 */
function onEdit(e) {
  try {
    var sheet = e.range.getSheet();
    var sheetName = sheet.getName();
    if (sheetName !== METADATA_SHEET_NAME) {
      console.log("onEdit triggered on sheet:", sheetName, "— updating timestamp");
      updateLastModified();
    }
  } catch (err) {
    console.error("onEdit error:", err);
  }
}

// ============================================================================
// Character Sheet Helpers
// ============================================================================

function getCharacterSheet(id, createIfMissing) {
  var spreadsheet = getSpreadsheet();
  var sheet = spreadsheet.getSheetByName(id);

  if (!sheet && createIfMissing) {
    console.log("Creating new character sheet:", id);
    sheet = spreadsheet.insertSheet(id);

    // Write character headers
    sheet.getRange(1, 1, 1, CHARACTER_HEADERS.length).setValues([CHARACTER_HEADERS]);
    sheet.getRange(1, 1, 1, CHARACTER_HEADERS.length)
      .setFontWeight("bold")
      .setBackground("#4285f4")
      .setFontColor("#ffffff");

    // Write item headers at row 4
    sheet.getRange(4, 1, 1, ITEM_HEADERS.length).setValues([ITEM_HEADERS]);
    sheet.getRange(4, 1, 1, ITEM_HEADERS.length)
      .setFontWeight("bold")
      .setBackground("#34a853")
      .setFontColor("#ffffff");

    console.log("Sheet created for character:", id);
  }

  return sheet;
}

// ============================================================================
// Read Operations
// ============================================================================

function handleGetCharacters() {
  console.log("handleGetCharacters() called");
  var spreadsheet = getSpreadsheet();
  var sheets = spreadsheet.getSheets();
  var characters = [];

  for (var i = 0; i < sheets.length; i++) {
    var sheet = sheets[i];
    var name = sheet.getName();
    if (name === METADATA_SHEET_NAME) continue;

    var values = sheet.getDataRange().getValues();
    if (values.length < 2) {
      console.log("Sheet", name, "has no character data, skipping");
      continue;
    }

    var rowValues = values[1]; // Row 2 (index 1)
    var character = rowToCharacter(rowValues);
    characters.push(character);
  }

  console.log("Returning", characters.length, "characters");
  return { success: true, data: characters };
}

function handleGetCharacter(id) {
  console.log("handleGetCharacter() called, id:", id);
  var sheet = getCharacterSheet(id, false);

  if (!sheet) {
    console.warn("Character sheet not found:", id);
    return { success: false, error: "Character not found: " + id };
  }

  var values = sheet.getDataRange().getValues();
  if (values.length < 2) {
    return { success: false, error: "Character sheet is empty: " + id };
  }

  // Read character from row 2
  var character = rowToCharacter(values[1]);

  // Read items from row 5 onward (index 4)
  var items = [];
  if (values.length > 4) {
    for (var i = 4; i < values.length; i++) {
      var item = rowToItem(values[i]);
      if (item && item.id) {
        items.push(item);
      }
    }
  }
  character.items = items;

  console.log("Character loaded:", character.name, "with", items.length, "items");
  return { success: true, data: character };
}

// ============================================================================
// Write Operations
// ============================================================================

function handleSaveCharacter(character) {
  console.log("handleSaveCharacter() called with id:", character ? character.id : "null");

  if (!character || !character.id) {
    console.error("Invalid character object — missing id");
    return { success: false, error: "Invalid character: missing id" };
  }

  var sheet = getCharacterSheet(character.id, true);
  var id = String(character.id).trim();

  // Write character data to row 2
  var charRow = characterToRow(character);
  sheet.getRange(2, 1, 1, CHARACTER_HEADERS.length).setValues([charRow]);

  // Write items
  var items = character.items || [];
  console.log("Writing", items.length, "items");

  // Clear existing item rows (from row 5 onward)
  var lastRow = sheet.getLastRow();
  if (lastRow > 4) {
    sheet.deleteRows(5, lastRow - 4);
  }

  // Write new item rows
  if (items.length > 0) {
    var itemRows = [];
    for (var i = 0; i < items.length; i++) {
      itemRows.push(itemToRow(items[i]));
    }
    sheet.getRange(5, 1, itemRows.length, ITEM_HEADERS.length).setValues(itemRows);
  }

  updateLastModified();
  console.log("Save complete for character:", id);
  return { success: true };
}

function handleDeleteCharacter(id) {
  console.log("handleDeleteCharacter() called, id:", id);
  var spreadsheet = getSpreadsheet();
  var sheet = spreadsheet.getSheetByName(String(id));

  if (sheet) {
    spreadsheet.deleteSheet(sheet);
    updateLastModified();
    console.log("Deleted sheet for character:", id);
    return { success: true };
  }

  console.warn("Character sheet not found for deletion:", id);
  return { success: false, error: "Character not found: " + id };
}

// ============================================================================
// Converters: Sheet Row ↔ Object
// ============================================================================

function rowToCharacter(row) {
  // Parse JSON columns for nested objects
  var appearance = {};
  var combat = {};
  var proficiencies = {};
  var weapons = [];
  var features = {};
  var skills = [];

  try { appearance = JSON.parse(row[19] || "{}"); } catch (e) { console.warn("Bad AppearanceJSON:", row[19]); }
  try { combat = JSON.parse(row[20] || "{}"); } catch (e) { console.warn("Bad CombatJSON:", row[20]); }
  try { proficiencies = JSON.parse(row[21] || "{}"); } catch (e) { console.warn("Bad ProficienciesJSON:", row[21]); }
  try { weapons = JSON.parse(row[22] || "[]"); } catch (e) { console.warn("Bad WeaponsJSON:", row[22]); }
  try { features = JSON.parse(row[23] || "{}"); } catch (e) { console.warn("Bad FeaturesJSON:", row[23]); }
  try { skills = JSON.parse(row[24] || "[]"); } catch (e) { console.warn("Bad SkillsJSON:", row[24]); }

  return {
    id: String(row[0] ?? ""),
    name: String(row[1] ?? ""),
    playerName: String(row[2] ?? ""),
    race: String(row[3] ?? ""),
    characterClass: String(row[4] ?? ""),
    subclass: String(row[16] ?? ""),
    background: String(row[17] ?? ""),
    level: Number(row[5]) || 0,
    experiencePoints: Number(row[18]) || 0,
    description: String(row[6] ?? ""),
    imageUrl: (row[7] && String(row[7]).trim()) ? String(row[7]).trim() : null,
    maxHp: Number(row[8]) || 0,
    currentHp: Number(row[9]) || 0,
    stats: {
      strength: Number(row[10]) || 0,
      dexterity: Number(row[11]) || 0,
      constitution: Number(row[12]) || 0,
      intelligence: Number(row[13]) || 0,
      wisdom: Number(row[14]) || 0,
      charisma: Number(row[15]) || 0
    },
    appearance: appearance,
    combat: combat,
    proficiencies: proficiencies,
    weapons: weapons,
    features: features,
    skills: skills,
    items: []
  };
}

function characterToRow(character) {
  var s = character.stats || {};
  return [
    String(character.id || ""),
    String(character.name || ""),
    String(character.playerName || ""),
    String(character.race || ""),
    String(character.characterClass || ""),
    Number(character.level) || 0,
    String(character.description || ""),
    String(character.imageUrl || ""),
    Number(character.maxHp) || 0,
    Number(character.currentHp) || 0,
    Number(s.strength) || 0,
    Number(s.dexterity) || 0,
    Number(s.constitution) || 0,
    Number(s.intelligence) || 0,
    Number(s.wisdom) || 0,
    Number(s.charisma) || 0,
    String(character.subclass || ""),
    String(character.background || ""),
    Number(character.experiencePoints) || 0,
    JSON.stringify(character.appearance || {}),
    JSON.stringify(character.combat || {}),
    JSON.stringify(character.proficiencies || {}),
    JSON.stringify(character.weapons || []),
    JSON.stringify(character.features || {}),
    JSON.stringify(character.skills || [])
  ];
}

function rowToItem(row) {
  var slotValue = row[2];
  var slot = (slotValue && String(slotValue).trim()) ? String(slotValue).trim() : null;

  var statsJson = row[4];
  var stats = {};
  if (statsJson && String(statsJson).trim()) {
    try {
      stats = JSON.parse(String(statsJson));
    } catch (e) {
      console.warn("Failed to parse item stats JSON:", statsJson, e);
    }
  }

  return {
    id: String(row[0] ?? ""),
    name: String(row[1] ?? ""),
    slot: slot,
    rarity: String(row[3] ?? "COMMON"),
    stats: stats,
    description: String(row[5] ?? ""),
    equipped: String(row[6]).toLowerCase() === "true" || row[6] === true || row[6] === 1,
    imageUrl: row[7] ? String(row[7]) : null
  };
}

function itemToRow(item) {
  var statsJson = "";
  if (item.stats && Object.keys(item.stats).length > 0) {
    try {
      statsJson = JSON.stringify(item.stats);
    } catch (e) {
      console.warn("Failed to stringify item stats:", item.stats);
    }
  }

  return [
    String(item.id || ""),
    String(item.name || ""),
    item.slot || "",
    String(item.rarity || "COMMON"),
    statsJson,
    String(item.description || ""),
    item.equipped ? "true" : "false",
    item.imageUrl || ""
  ];
}
