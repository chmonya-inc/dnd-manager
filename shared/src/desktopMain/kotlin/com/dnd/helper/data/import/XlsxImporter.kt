package com.dnd.helper.data.import

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream

class XlsxImporter(private val repository: CharacterRepository) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    suspend fun import(filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            FileInputStream(File(filePath)).use { fis ->
                val workbook = XSSFWorkbook(fis)
                
                // 1. Import Locations
                importLocations(workbook)
                
                // 2. Import Monsters
                importMonsters(workbook)
                
                // 3. Import NPCs
                importNpcs(workbook)
                
                // 4. Import Music
                importMusic(workbook)
                
                // 5. Import Characters (Summary and detailed items)
                importCharacters(workbook)
                
                Result.Success(Unit)
            }
        } catch (e: Exception) {
            println("[XlsxImporter] Error: ${e.message}")
            e.printStackTrace()
            Result.Error(com.dnd.helper.domain.common.AppError.Unknown(e.message ?: "Unknown error"))
        }
    }

    private suspend fun importLocations(workbook: Workbook) {
        val sheet = workbook.getSheet("Locations") ?: return
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val id = row.getCell(0)?.getVal() ?: continue
            val name = row.getCell(1)?.getVal() ?: ""
            val desc = row.getCell(2)?.getVal() ?: ""
            val img = row.getCell(3)?.getVal() ?: ""
            repository.saveLocation(Location(id, name, desc, img))
        }
    }

    private suspend fun importMonsters(workbook: Workbook) {
        val sheet = workbook.getSheet("Monsters") ?: return
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val id = row.getCell(0)?.getVal() ?: continue
            val name = row.getCell(1)?.getVal() ?: ""
            val desc = row.getCell(2)?.getVal() ?: ""
            val img = row.getCell(3)?.getVal() ?: ""
            val statsJson = row.getCell(4)?.getVal() ?: "{}"
            val stats = try { json.decodeFromString<CharacterStats>(statsJson) } catch (_: Exception) { CharacterStats() }
            val maxHp = row.getCell(5)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10
            val curHp = row.getCell(6)?.getVal()?.toDoubleOrNull()?.toInt() ?: maxHp
            val ac = row.getCell(7)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10
            val speed = row.getCell(8)?.getVal()?.toDoubleOrNull()?.toInt() ?: 30
            val cr = row.getCell(9)?.getVal() ?: "1"
            val type = row.getCell(10)?.getVal() ?: "Humanoid"
            val align = row.getCell(11)?.getVal() ?: "Neutral"
            val size = row.getCell(12)?.getVal() ?: "Medium"
            
            repository.saveMonster(Monster(id, name, desc, img, stats, maxHp, curHp, ac, speed, cr, type, align, size))
        }
    }

    private suspend fun importNpcs(workbook: Workbook) {
        val sheet = workbook.getSheet("Npcs") ?: return
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val id = row.getCell(0)?.getVal() ?: continue
            val name = row.getCell(1)?.getVal() ?: ""
            val desc = row.getCell(2)?.getVal() ?: ""
            val img = row.getCell(3)?.getVal() ?: ""
            val bg = row.getCell(4)?.getVal() ?: ""
            repository.saveNpc(Npc(id, name, desc, img, bg))
        }
    }

    private suspend fun importMusic(workbook: Workbook) {
        val sheet = workbook.getSheet("Music") ?: return
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val id = row.getCell(0)?.getVal() ?: continue
            val name = row.getCell(1)?.getVal() ?: ""
            val url = row.getCell(2)?.getVal() ?: ""
            repository.saveMusic(MusicTrack(id, name, url))
        }
    }

    private suspend fun importCharacters(workbook: Workbook) {
        val sheet = workbook.getSheet("CharactersSummary") ?: return
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val id = row.getCell(0)?.getVal() ?: continue
            if (id == "ID") continue
            
            val name = row.getCell(1)?.getVal() ?: ""
            val playerName = row.getCell(2)?.getVal() ?: ""
            val race = row.getCell(3)?.getVal() ?: ""
            val charClass = row.getCell(4)?.getVal() ?: ""
            val level = row.getCell(5)?.getVal()?.toDoubleOrNull()?.toInt() ?: 1
            val desc = row.getCell(6)?.getVal() ?: ""
            val img = row.getCell(7)?.getVal() ?: ""
            val maxHp = row.getCell(8)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10
            val curHp = row.getCell(9)?.getVal()?.toDoubleOrNull()?.toInt() ?: maxHp
            
            val stats = CharacterStats(
                strength = row.getCell(10)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10,
                dexterity = row.getCell(11)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10,
                constitution = row.getCell(12)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10,
                intelligence = row.getCell(13)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10,
                wisdom = row.getCell(14)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10,
                charisma = row.getCell(15)?.getVal()?.toDoubleOrNull()?.toInt() ?: 10
            )
            
            val subclass = row.getCell(16)?.getVal() ?: ""
            val bg = row.getCell(17)?.getVal() ?: ""
            val xp = row.getCell(18)?.getVal()?.toDoubleOrNull()?.toInt() ?: 0
            
            val appearance = try { json.decodeFromString<CharacterAppearance>(row.getCell(19)?.getVal() ?: "{}") } catch (e: Exception) { CharacterAppearance() }
            val combat = try { json.decodeFromString<CharacterCombat>(row.getCell(20)?.getVal() ?: "{}") } catch (e: Exception) { CharacterCombat() }
            val profs = try { json.decodeFromString<CharacterProficiencies>(row.getCell(21)?.getVal() ?: "{}") } catch (e: Exception) { CharacterProficiencies() }
            val weapons = try { json.decodeFromString<List<Weapon>>(row.getCell(22)?.getVal() ?: "[]") } catch (e: Exception) { emptyList() }
            val features = try { json.decodeFromString<CharacterFeatures>(row.getCell(23)?.getVal() ?: "{}") } catch (e: Exception) { CharacterFeatures() }
            val skills = try { json.decodeFromString<List<Skill>>(row.getCell(24)?.getVal() ?: "[]") } catch (e: Exception) { emptyList() }
            
            // Try to fetch items from individual sheet first
            val items = fetchItemsFromSheet(workbook, id) ?: try { 
                json.decodeFromString<List<Item>>(row.getCell(25)?.getVal() ?: "[]") 
            } catch (e: Exception) { emptyList() }
            
            val notes = try { json.decodeFromString<List<Note>>(row.getCell(26)?.getVal() ?: "[]") } catch (e: Exception) { emptyList() }
            
            val character = Character(
                id = id, name = name, playerName = playerName, race = race, characterClass = charClass,
                subclass = subclass, background = bg, level = level, experiencePoints = xp,
                description = desc, imageUrl = img, appearance = appearance, stats = stats,
                maxHp = maxHp, currentHp = curHp, combat = combat, proficiencies = profs,
                weapons = weapons, features = features, skills = skills, items = items, notes = notes
            )
            
            repository.saveCharacter(character)
        }
    }

    private fun fetchItemsFromSheet(workbook: Workbook, charId: String): List<Item>? {
        val sheet = workbook.getSheet(charId) ?: return null
        val items = mutableListOf<Item>()
        // Items start at row 5 (index 4) based on Code.gs logic
        for (j in 4..sheet.lastRowNum) {
            val itemRow = sheet.getRow(j) ?: continue
            val itemId = itemRow.getCell(0)?.getVal() ?: continue
            val itemName = itemRow.getCell(1)?.getVal() ?: ""
            val slot = try { EquipmentSlot.valueOf(itemRow.getCell(2)?.getVal() ?: "") } catch (e: Exception) { null }
            val rarity = try { ItemRarity.valueOf(itemRow.getCell(3)?.getVal() ?: "COMMON") } catch (e: Exception) { ItemRarity.COMMON }
            val itemStats = try { json.decodeFromString<Map<String, Int>>(itemRow.getCell(4)?.getVal() ?: "{}") } catch (e: Exception) { emptyMap() }
            val itemDesc = itemRow.getCell(5)?.getVal() ?: ""
            val equipped = itemRow.getCell(6)?.getVal()?.lowercase() == "true"
            val itemImg = itemRow.getCell(7)?.getVal()
            
            items.add(Item(itemId, itemName, slot, rarity, itemStats, itemDesc, equipped, itemImg))
        }
        return items
    }

    private fun Cell.getVal(): String? {
        return when (cellType) {
            CellType.STRING -> stringCellValue
            CellType.NUMERIC -> numericCellValue.toString()
            CellType.BOOLEAN -> booleanCellValue.toString()
            CellType.FORMULA -> cellFormula
            else -> null
        }
    }
}
