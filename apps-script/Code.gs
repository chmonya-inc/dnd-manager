/**
 * D&D Helper — Google Apps Script Backend
 *
 * This script acts as a proxy between the Kotlin Multiplatform app and Google Sheets.
 * Deploy it as a Web App (Deploy → New deployment → Web app) with:
 *   - Execute as: Me
 *   - Who has access: Anyone
 *
 * SPREADSHEET_ID:
 *   - Leave empty ("") to use the spreadsheet this script is bound to.
 *   - Paste a Spreadsheet ID to target a specific sheet (script can be standalone).
 *   - Spreadsheet ID is the long string in the URL:
 *     https://docs.google.com/spreadsheets/d/SPREADSHEET_ID/edit
 *
 * IMPORTANT: This script receives requests via GET (not POST).
 * Google Apps Script Web Apps redirect POST requests, dropping the body.
 * The Kotlin app sends the JSON request as a URL query parameter: ?request=JSON
 *
 * DEBUG: View logs at https://script.google.com (choose your project → Executions)
 */

const SPREADSHEET_ID = ""; // <-- Paste your Spreadsheet ID here, or leave empty
const SHEET_NAME = "Characters";
const METADATA_SHEET_NAME = "Metadata";
const HEADERS = [
  "ID", "Name", "PlayerName", "Race", "Class", "Level",
  "Description", "ImageUrl", "MaxHP", "CurrentHP",
  "Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma"
];

/**
 * Main entry point for GET requests from the Kotlin app.
 * The request JSON is passed in the ?request= URL query parameter.
 */
function doGet(e) {
  console.log("=== doGet START ===");
  console.log("Query parameters:", JSON.stringify(e.parameter));

  var lock = LockService.getScriptLock();
  try {
    // Wait for up to 30 seconds for other processes to finish.
    lock.waitLock(30000);

    var requestJson = e.parameter.request;
    if (!requestJson) {
      console.error("Missing 'request' query parameter");
      return jsonOutput({ success: false, error: "Missing 'request' query parameter" });
    }

    var request = JSON.parse(requestJson);
    console.log("Parsed request:", JSON.stringify(request));
    var result = handleRequest(request);

    // Ensure all changes are committed before returning
    SpreadsheetApp.flush();

    console.log("Result:", JSON.stringify(result));
    console.log("=== doGet END ===");
    return jsonOutput(result);
  } catch (error) {
    console.error("CRITICAL ERROR in doGet:", error);
    console.error("Stack:", error.stack);
    return jsonOutput({ success: false, error: error.toString(), stack: error.stack });
  } finally {
    lock.releaseLock();
  }
}

/**
 * Kept for backward compatibility (e.g., browser form submissions).
 * For the Kotlin app, doGet is used exclusively.
 */
function doPost(e) {
  console.log("=== doPost START ===");

  var lock = LockService.getScriptLock();
  lock.waitLock(30000);

  try {
    var request = JSON.parse(e.postData.contents);
    console.log("Parsed request:", JSON.stringify(request));
    var result = handleRequest(request);

    console.log("=== doPost END ===");
    return jsonOutput(result);
  } catch (error) {
    console.error("CRITICAL ERROR in doPost:", error);
    console.error("Stack:", error.stack);
    return jsonOutput({ success: false, error: error.toString(), stack: error.stack });
  } finally {
    lock.releaseLock();
  }
}

/**
 * Shared request router used by both doGet and doPost.
 */
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
      console.log("Routing to handleSaveCharacter, character:", JSON.stringify(request.character));
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

/**
 * Returns a JSON text output for the Web App response.
 */
function jsonOutput(data) {
  console.log("jsonOutput:", JSON.stringify(data).substring(0, 500));
  return ContentService
    .createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
}

/**
 * Opens the target spreadsheet.
 * Uses SPREADSHEET_ID if set, otherwise falls back to the active (bound) spreadsheet.
 */
function getSpreadsheet() {
  if (SPREADSHEET_ID && SPREADSHEET_ID.trim() !== "") {
    console.log("Opening spreadsheet by ID:", SPREADSHEET_ID);
    return SpreadsheetApp.openById(SPREADSHEET_ID);
  }
  console.log("Using active (bound) spreadsheet");
  return SpreadsheetApp.getActiveSpreadsheet();
}

/**
 * Gets the "Characters" sheet, creating it with headers if it doesn't exist.
 */
function getSheet() {
  console.log("getSheet() called");
  var spreadsheet = getSpreadsheet();
  var sheet = spreadsheet.getSheetByName(SHEET_NAME);

  if (!sheet) {
    console.log("Sheet not found, creating new sheet:", SHEET_NAME);
    sheet = spreadsheet.insertSheet(SHEET_NAME);
    sheet.appendRow(HEADERS);

    // Format header row as bold
    sheet.getRange(1, 1, 1, HEADERS.length)
      .setFontWeight("bold")
      .setBackground("#4285f4")
      .setFontColor("#ffffff");

    console.log("Sheet created with headers:", HEADERS.join(", "));
  } else {
    console.log("Sheet found:", SHEET_NAME);
  }

  return sheet;
}

/**
 * Gets the "Metadata" sheet, creating it if it doesn't exist.
 * Stores a single timestamp in B1 representing the last time any character was modified.
 */
function getMetadataSheet() {
  var spreadsheet = getSpreadsheet();
  var sheet = spreadsheet.getSheetByName(METADATA_SHEET_NAME);

  if (!sheet) {
    sheet = spreadsheet.insertSheet(METADATA_SHEET_NAME);
    sheet.getRange("A1").setValue("lastModified");
    sheet.getRange("B1").setValue(new Date().toISOString());
  }
  return sheet;
}

/**
 * Updates the last-modified timestamp in the Metadata sheet.
 */
function updateLastModified() {
  var sheet = getMetadataSheet();
  sheet.getRange("B1").setValue(new Date().toISOString());
  SpreadsheetApp.flush();
}

/**
 * Returns the current last-modified timestamp.
 * Clients poll this lightweight endpoint to detect changes.
 */
function handleGetLastModified() {
  var sheet = getMetadataSheet();
  var timestamp = sheet.getRange("B1").getValue();
  return { success: true, data: timestamp ? timestamp.toString() : new Date().toISOString() };
}

/**
 * Reads all characters from the sheet.
 */
function handleGetCharacters() {
  console.log("handleGetCharacters() called");
  var sheet = getSheet();
  var values = sheet.getDataRange().getValues();
  console.log("Total rows in sheet (including header):", values.length);

  var characters = [];

  // Skip header row (index 0)
  for (var i = 1; i < values.length; i++) {
    characters.push(rowToCharacter(values[i]));
  }

  console.log("Returning", characters.length, "characters");
  return { success: true, data: characters };
}

/**
 * Reads a single character by ID.
 */
function handleGetCharacter(id) {
  console.log("handleGetCharacter() called, id:", id);
  var sheet = getSheet();
  var values = sheet.getDataRange().getValues();
  console.log("Searching in", values.length, "rows");

  for (var i = 1; i < values.length; i++) {
    if (values[i][0] == id) {
      console.log("Character found at row", i + 1);
      return { success: true, data: rowToCharacter(values[i]) };
    }
  }

  console.warn("Character not found:", id);
  return { success: false, error: "Character not found: " + id };
}

/**
 * Saves (inserts or updates) a character.
 */
function handleSaveCharacter(character) {
  console.log("handleSaveCharacter() called with:", JSON.stringify(character));

  if (!character || !character.id) {
    console.error("Invalid character object — missing id");
    return { success: false, error: "Invalid character: missing id" };
  }

  var sheet = getSheet();
  var values = sheet.getDataRange().getValues();
  var searchId = String(character.id).trim();
  console.log("Searching for ID:", searchId);

  // Try to find existing row by ID and update it
  for (var i = 1; i < values.length; i++) {
    var currentRowId = String(values[i][0]).trim();
    if (currentRowId === searchId) {
      var rowIndex = i + 1; // Sheets are 1-indexed
      console.log("Updating existing character at row", rowIndex);
      var rowData = characterToRow(character);
      console.log("Writing row data:", JSON.stringify(rowData));
      sheet.getRange(rowIndex, 1, 1, HEADERS.length)
        .setValues([rowData]);

      SpreadsheetApp.flush();
      updateLastModified();
      console.log("Update complete");
      return { success: true };
    }
  }

  // Not found — append as new row
  console.log("Character not found, appending new row");
  var newRowData = characterToRow(character);
  sheet.appendRow(newRowData);
  SpreadsheetApp.flush();
  updateLastModified();
  console.log("Append complete");
  return { success: true };
}

/**
 * Deletes a character by ID.
 */
function handleDeleteCharacter(id) {
  console.log("handleDeleteCharacter() called, id:", id);
  var sheet = getSheet();
  var values = sheet.getDataRange().getValues();

  for (var i = 1; i < values.length; i++) {
    if (values[i][0] == id) {
      console.log("Deleting character at row", i + 1);
      sheet.deleteRow(i + 1); // Sheets are 1-indexed
      updateLastModified();
      console.log("Delete complete");
      return { success: true };
    }
  }

  console.warn("Character not found for deletion:", id);
  return { success: false, error: "Character not found: " + id };
}

/**
 * Converts a sheet row array into a Character JSON object.
 */
function rowToCharacter(row) {
  return {
    id: String(row[0] ?? ""),
    name: String(row[1] ?? ""),
    playerName: String(row[2] ?? ""),
    race: String(row[3] ?? ""),
    characterClass: String(row[4] ?? ""),
    level: Number(row[5]) || 0,
    description: String(row[6] ?? ""),
    imageUrl: row[7] ? String(row[7]) : null,
    maxHp: Number(row[8]) || 0,
    currentHp: Number(row[9]) || 0,
    stats: {
      strength: Number(row[10]) || 0,
      dexterity: Number(row[11]) || 0,
      constitution: Number(row[12]) || 0,
      intelligence: Number(row[13]) || 0,
      wisdom: Number(row[14]) || 0,
      charisma: Number(row[15]) || 0
    }
  };
}

/**
 * Converts a Character JSON object into a sheet row array.
 */
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
    Number(s.charisma) || 0
  ];
}
