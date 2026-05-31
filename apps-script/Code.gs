/**
 * D&D Helper — Google Apps Script Backend (Optimized + Items Support)
 */

const SPREADSHEET_ID = "";
const METADATA_SHEET_NAME = "Metadata";
const LOCATION_SHEET_NAME = "Locations";
const MONSTER_SHEET_NAME = "Monsters";
const NPC_SHEET_NAME = "Npcs";
const SUMMARY_SHEET_NAME = "CharactersSummary";
const LOG_SHEET_NAME = "Logs";

const CHARACTER_HEADERS = [
  "ID", "Name", "PlayerName", "Race", "Class", "Level",
  "Description", "ImageUrl", "MaxHP", "CurrentHP",
  "Strength", "Dexterity", "Constitution", "Intelligence", "Wisdom", "Charisma",
  "Subclass", "Background", "ExperiencePoints",
  "AppearanceJSON", "CombatJSON", "ProficienciesJSON", "WeaponsJSON", "FeaturesJSON",
  "SkillsJSON", "ItemsJSON"
];

const ITEM_HEADERS = ["ItemID", "ItemName", "Slot", "Rarity", "StatsJSON", "Description", "Equipped", "ImageUrl"];
const LOCATION_HEADERS = ["ID", "Name", "Description", "ImageUrl"];
const MONSTER_HEADERS = ["ID", "Name", "Description", "ImageUrl", "StatsJSON", "MaxHP", "CurrentHP", "ArmorClass", "Speed", "CR", "Type", "Alignment", "Size"];
const NPC_HEADERS = ["ID", "Name", "Description", "ImageUrl", "Background"];
const LOG_HEADERS = ["Timestamp", "Action", "Details", "InitialState", "EndState", "Success"];

const WRITE_ACTIONS = ["saveCharacter", "deleteCharacter", "saveLocation", "deleteLocation", "saveMonster", "deleteMonster", "saveNpc", "deleteNpc", "saveLog"];
const READ_ACTIONS = ["getCharacters", "getCharacter", "getLastModified", "getLocations", "getMonsters", "getNpcs", "getInitialData", "getLogs"];

function doGet(e) {
  var requestJson = e.parameter.request;
  if (!requestJson) return jsonOutput({ success: false, error: "Missing request" });
  var request = JSON.parse(requestJson);
  var action = request.action;

  try {
    var result;
    if (WRITE_ACTIONS.indexOf(action) >= 0) {
      var lock = LockService.getScriptLock();
      lock.waitLock(30000);
      try {
        result = handleRequest(request);
        SpreadsheetApp.flush();
      } finally {
        lock.releaseLock();
      }
    } else {
      result = handleRequest(request);
    }
    return jsonOutput(result);
  } catch (error) {
    return jsonOutput({ success: false, error: error.toString() });
  }
}

function handleRequest(request) {
  switch (request.action) {
    case "getInitialData": return handleGetInitialData();
    case "getCharacters": return handleGetCharacters();
    case "getCharacter": return handleGetCharacter(request.id);
    case "saveCharacter": return handleSaveCharacter(request.character);
    case "deleteCharacter": return handleDeleteCharacter(request.id);
    case "getLastModified": return handleGetLastModified();
    case "getLocations": return handleGetLocations();
    case "saveLocation": return handleSaveLocation(request.location);
    case "deleteLocation": return handleDeleteLocation(request.id);
    case "getMonsters": return handleGetMonsters();
    case "saveMonster": return handleSaveMonster(request.monster);
    case "deleteMonster": return handleDeleteMonster(request.id);
    case "getNpcs": return handleGetNpcs();
    case "saveNpc": return handleSaveNpc(request.npc);
    case "deleteNpc": return handleDeleteNpc(request.id);
    case "saveLog": return handleSaveLog(request.log);
    case "getLogs": return handleGetLogs();
    default: return { success: false, error: "Unknown action" };
  }
}

function jsonOutput(data) {
  return ContentService.createTextOutput(JSON.stringify(data)).setMimeType(ContentService.MimeType.JSON);
}

function getSpreadsheet() {
  return SPREADSHEET_ID ? SpreadsheetApp.openById(SPREADSHEET_ID) : SpreadsheetApp.getActiveSpreadsheet();
}

function getSummarySheet() {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(SUMMARY_SHEET_NAME);
  if (!sheet) {
    sheet = ss.insertSheet(SUMMARY_SHEET_NAME);
    sheet.getRange(1, 1, 1, CHARACTER_HEADERS.length).setValues([CHARACTER_HEADERS]);
    sheet.setFrozenRows(1);
  }
  return sheet;
}

function handleGetInitialData() {
  return {
    success: true,
    data: {
      characters: handleGetCharacters().data,
      locations: handleGetLocations().data,
      monsters: handleGetMonsters().data,
      npcs: handleGetNpcs().data,
      lastModified: handleGetLastModified().data
    }
  };
}

function handleGetCharacters() {
  var sheet = getSummarySheet();
  var values = sheet.getDataRange().getValues();
  if (values.length < 2) return { success: true, data: [] };

  var ss = getSpreadsheet();
  var characters = [];
  for (var i = 1; i < values.length; i++) {
    var charId = values[i][0];
    if (charId && charId != "ID") {
      var character = rowToCharacter(values[i]);

      // Fetch fresh items from the character's individual sheet if it exists
      // because the Summary sheet's ItemsJSON might be outdated or incomplete.
      var charSheet = ss.getSheetByName(charId);
      if (charSheet) {
        var charValues = charSheet.getDataRange().getValues();
        if (charValues.length > 4) {
          var items = [];
          for (var j = 4; j < charValues.length; j++) {
            var itemRow = charValues[j];
            if (itemRow[0]) {
              items.push({
                id: String(itemRow[0]),
                name: String(itemRow[1]),
                slot: itemRow[2] || null,
                rarity: String(itemRow[3] || "COMMON"),
                stats: JSON.parse(itemRow[4] || "{}"),
                description: String(itemRow[5]),
                equipped: String(itemRow[6]) === "true",
                imageUrl: itemRow[7] || null
              });
            }
          }
          character.items = items;
        }
      }

      characters.push(character);
    }
  }
  return { success: true, data: characters };
}

function handleSaveCharacter(character) {
  if (!character || !character.id) return { success: false, error: "Invalid ID" };

  var ss = getSpreadsheet();
  var charSheet = ss.getSheetByName(character.id) || ss.insertSheet(character.id);

  if (charSheet.getLastRow() === 0) {
    charSheet.getRange(1, 1, 1, CHARACTER_HEADERS.length).setValues([CHARACTER_HEADERS]);
    charSheet.getRange(4, 1, 1, ITEM_HEADERS.length).setValues([ITEM_HEADERS]);
  }

  charSheet.getRange(2, 1, 1, CHARACTER_HEADERS.length - 1).setValues([characterToRow(character).slice(0, -1)]);

  var lastRow = charSheet.getLastRow();
  if (lastRow > 4) charSheet.deleteRows(5, lastRow - 4);
  var items = character.items || [];
  if (items.length > 0) {
    charSheet.getRange(5, 1, items.length, ITEM_HEADERS.length).setValues(items.map(itemToRow));
  }

  var summarySheet = getSummarySheet();
  var summaryData = summarySheet.getDataRange().getValues();
  var rowIndex = -1;
  for (var i = 1; i < summaryData.length; i++) {
    if (summaryData[i][0] == character.id) { rowIndex = i + 1; break; }
  }

  var row = characterToRow(character);
  if (rowIndex > 0) {
    summarySheet.getRange(rowIndex, 1, 1, CHARACTER_HEADERS.length).setValues([row]);
  } else {
    summarySheet.appendRow(row);
  }

  updateLastModified();
  return { success: true };
}

function rowToCharacter(row) {
  var appearance = {}; var combat = {}; var proficiencies = {}; var weapons = []; var features = {}; var skills = []; var items = [];
  try { appearance = JSON.parse(row[19] || "{}"); } catch (e) {}
  try { combat = JSON.parse(row[20] || "{}"); } catch (e) {}
  try { proficiencies = JSON.parse(row[21] || "{}"); } catch (e) {}
  try { weapons = JSON.parse(row[22] || "[]"); } catch (e) {}
  try { features = JSON.parse(row[23] || "{}"); } catch (e) {}
  try { skills = JSON.parse(row[24] || "[]"); } catch (e) {}
  try { items = JSON.parse(row[25] || "[]"); } catch (e) {}

  return {
    id: String(row[0] || ""), name: String(row[1] || ""), playerName: String(row[2] || ""),
    race: String(row[3] || ""), characterClass: String(row[4] || ""),
    level: Number(row[5]) || 0, description: String(row[6] || ""),
    imageUrl: (row[7] && String(row[7]).trim()) ? String(row[7]).trim() : null,
    maxHp: Number(row[8]) || 0, currentHp: Number(row[9]) || 0,
    stats: {
      strength: Number(row[10]) || 0, dexterity: Number(row[11]) || 0, constitution: Number(row[12]) || 0,
      intelligence: Number(row[13]) || 0, wisdom: Number(row[14]) || 0, charisma: Number(row[15]) || 0
    },
    subclass: String(row[16] || ""), background: String(row[17] || ""),
    experiencePoints: Number(row[18]) || 0,
    appearance: appearance, combat: combat, proficiencies: proficiencies,
    weapons: weapons, features: features, skills: skills, items: items
  };
}

function characterToRow(character) {
  var s = character.stats || {};
  return [
    String(character.id || ""), String(character.name || ""), String(character.playerName || ""),
    String(character.race || ""), String(character.characterClass || ""), Number(character.level) || 0,
    String(character.description || ""), String(character.imageUrl || ""), Number(character.maxHp) || 0,
    Number(character.currentHp) || 0, Number(s.strength) || 0, Number(s.dexterity) || 0,
    Number(s.constitution) || 0, Number(s.intelligence) || 0, Number(s.wisdom) || 0, Number(s.charisma) || 0,
    String(character.subclass || ""), String(character.background || ""), Number(character.experiencePoints) || 0,
    JSON.stringify(character.appearance || {}), JSON.stringify(character.combat || {}),
    JSON.stringify(character.proficiencies || {}), JSON.stringify(character.weapons || []),
    JSON.stringify(character.features || {}), JSON.stringify(character.skills || []),
    JSON.stringify(character.items || [])
  ];
}

function itemToRow(item) {
  return [String(item.id || ""), String(item.name || ""), item.slot || "", String(item.rarity || "COMMON"), JSON.stringify(item.stats || {}), String(item.description || ""), item.equipped ? "true" : "false", item.imageUrl || ""];
}

function handleGetCharacter(id) {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(id);
  if (!sheet) return { success: false, error: "Not found" };
  var values = sheet.getDataRange().getValues();
  if (values.length < 2) return { success: false, error: "Empty" };
  var character = rowToCharacter(values[1].concat([JSON.stringify([])])); // items read from rows
  var items = [];
  if (values.length > 4) {
    for (var i = 4; i < values.length; i++) {
      var itemRow = values[i];
      if (itemRow[0]) items.push({
        id: String(itemRow[0]), name: String(itemRow[1]), slot: itemRow[2] || null,
        rarity: String(itemRow[3] || "COMMON"), stats: JSON.parse(itemRow[4] || "{}"),
        description: String(itemRow[5]), equipped: String(itemRow[6]) === "true",
        imageUrl: itemRow[7] || null
      });
    }
  }
  character.items = items;
  return { success: true, data: character };
}

function handleDeleteCharacter(id) {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(id);
  if (sheet) ss.deleteSheet(sheet);

  var summary = getSummarySheet();
  var data = summary.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) {
    if (data[i][0] == id) { summary.deleteRow(i + 1); break; }
  }
  updateLastModified();
  return { success: true };
}

function updateLastModified() {
  var sheet = getSpreadsheet().getSheetByName(METADATA_SHEET_NAME) || getSpreadsheet().insertSheet(METADATA_SHEET_NAME);
  sheet.getRange("A1").setValue("lastModified");
  sheet.getRange("B1").setValue(new Date().toISOString());
}

function handleGetLastModified() {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(METADATA_SHEET_NAME);
  var ts = sheet ? sheet.getRange("B1").getValue() : new Date().toISOString();
  return { success: true, data: ts.toString() };
}

function getLocationsSheet() {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(LOCATION_SHEET_NAME) || ss.insertSheet(LOCATION_SHEET_NAME);
  if (sheet.getLastRow() === 0) sheet.appendRow(LOCATION_HEADERS);
  return sheet;
}
function handleGetLocations() {
  var values = getLocationsSheet().getDataRange().getValues();
  var data = [];
  for (var i = 1; i < values.length; i++) {
    if (values[i][0]) data.push({ id: String(values[i][0]), name: values[i][1], description: values[i][2], imageUrl: values[i][3] });
  }
  return { success: true, data: data };
}
function handleSaveLocation(loc) {
  var sheet = getLocationsSheet();
  var data = sheet.getDataRange().getValues();
  var idx = -1;
  for (var i = 1; i < data.length; i++) if (data[i][0] == loc.id) { idx = i + 1; break; }
  var row = [loc.id, loc.name, loc.description, loc.imageUrl];
  if (idx > 0) sheet.getRange(idx, 1, 1, 4).setValues([row]); else sheet.appendRow(row);
  updateLastModified();
  return { success: true };
}
function handleDeleteLocation(id) {
  var sheet = getLocationsSheet();
  var data = sheet.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) if (data[i][0] == id) { sheet.deleteRow(i + 1); break; }
  updateLastModified();
  return { success: true };
}

function getMonstersSheet() {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(MONSTER_SHEET_NAME) || ss.insertSheet(MONSTER_SHEET_NAME);
  if (sheet.getLastRow() === 0) sheet.appendRow(MONSTER_HEADERS);
  return sheet;
}
function handleGetMonsters() {
  var values = getMonstersSheet().getDataRange().getValues();
  var data = [];
  for (var i = 1; i < values.length; i++) {
    if (values[i][0]) data.push({
      id: String(values[i][0]), name: values[i][1], description: values[i][2], imageUrl: values[i][3],
      stats: JSON.parse(values[i][4] || "{}"), maxHp: values[i][5], currentHp: values[i][6],
      armorClass: values[i][7], speed: values[i][8], challengeRating: values[i][9],
      type: values[i][10], alignment: values[i][11], size: values[i][12]
    });
  }
  return { success: true, data: data };
}
function handleSaveMonster(m) {
  var sheet = getMonstersSheet();
  var data = sheet.getDataRange().getValues();
  var idx = -1;
  for (var i = 1; i < data.length; i++) if (data[i][0] == m.id) { idx = i + 1; break; }
  var row = [m.id, m.name, m.description, m.imageUrl, JSON.stringify(m.stats), m.maxHp, m.currentHp, m.armorClass, m.speed, m.challengeRating, m.type, m.alignment, m.size];
  if (idx > 0) sheet.getRange(idx, 1, 1, row.length).setValues([row]); else sheet.appendRow(row);
  updateLastModified();
  return { success: true };
}
function handleDeleteMonster(id) {
  var sheet = getMonstersSheet();
  var data = sheet.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) if (data[i][0] == id) { sheet.deleteRow(i + 1); break; }
  updateLastModified();
  return { success: true };
}

function getNpcsSheet() {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(NPC_SHEET_NAME) || ss.insertSheet(NPC_SHEET_NAME);
  if (sheet.getLastRow() === 0) sheet.appendRow(NPC_HEADERS);
  return sheet;
}
function handleGetNpcs() {
  var values = getNpcsSheet().getDataRange().getValues();
  var data = [];
  for (var i = 1; i < values.length; i++) {
    if (values[i][0]) data.push({ id: String(values[i][0]), name: values[i][1], description: values[i][2], imageUrl: values[i][3], background: values[i][4] });
  }
  return { success: true, data: data };
}
function handleSaveNpc(n) {
  var sheet = getNpcsSheet();
  var data = sheet.getDataRange().getValues();
  var idx = -1;
  for (var i = 1; i < data.length; i++) if (data[i][0] == n.id) { idx = i + 1; break; }
  var row = [n.id, n.name, n.description, n.imageUrl, n.background];
  if (idx > 0) sheet.getRange(idx, 1, 1, 5).setValues([row]); else sheet.appendRow(row);
  updateLastModified();
  return { success: true };
}
function handleDeleteNpc(id) {
  var sheet = getNpcsSheet();
  var data = sheet.getDataRange().getValues();
  for (var i = 1; i < data.length; i++) if (data[i][0] == id) { sheet.deleteRow(i + 1); break; }
  updateLastModified();
  return { success: true };
}

function getLogsSheet() {
  var ss = getSpreadsheet();
  var sheet = ss.getSheetByName(LOG_SHEET_NAME) || ss.insertSheet(LOG_SHEET_NAME);
  if (sheet.getLastRow() === 0) sheet.appendRow(LOG_HEADERS);
  return sheet;
}

function handleSaveLog(log) {
  var sheet = getLogsSheet();
  // Use server time if timestamp is missing or mocked
  var timestamp = log.timestamp;
  if (!timestamp || timestamp.indexOf("2024-05-20") === 0) {
    timestamp = new Date().toISOString();
  }
  var row = [
    timestamp,
    log.action,
    log.details || "",
    log.initialState || "",
    log.endState || "",
    log.success !== false
  ];
  sheet.appendRow(row);
  return { success: true };
}

function handleGetLogs() {
  var values = getLogsSheet().getDataRange().getValues();
  var data = [];
  // Return last 100 logs
  var start = Math.max(1, values.length - 100);
  for (var i = values.length - 1; i >= start; i--) {
    if (values[i][0]) data.push({
      timestamp: values[i][0],
      action: values[i][1],
      details: values[i][2],
      initialState: values[i][3],
      endState: values[i][4],
      success: values[i][5]
    });
  }
  return { success: true, data: data };
}
