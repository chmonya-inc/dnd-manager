package com.dnd.helper.data.repository

import com.dnd.helper.data.remote.GoogleAppsScriptDataSource
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.domain.model.Npc
import com.dnd.helper.domain.repository.CharacterRepository

class CharacterRepositoryImpl(
    private val dataSource: GoogleAppsScriptDataSource,
) : CharacterRepository {

    // Simple in-memory caches to make UI instant
    private var charactersCache: List<Character>? = null
    private val heavyCharacterCache = mutableMapOf<String, Character>()
    private var locationsCache: List<Location>? = null
    private var monstersCache: List<Monster>? = null
    private var npcsCache: List<Npc>? = null

    override suspend fun getInitialData(): Result<com.dnd.helper.domain.model.InitialData> {
        val result = dataSource.getInitialData()
        if (result is Result.Success) {
            val data = result.data
            
            // Populate all caches
            val filteredChars = data.characters.filter { it.id != "ID" }
            charactersCache = filteredChars
            locationsCache = data.locations
            monstersCache = data.monsters
            npcsCache = data.npcs
            
            // Update heavy character cache for any characters that have items
            filteredChars.forEach { char ->
                if (char.items.isNotEmpty()) {
                    heavyCharacterCache[char.id] = char
                }
            }
        }
        return result
    }

    override suspend fun getCharacters(forceRefresh: Boolean): Result<List<Character>> {
        // Return cached data immediately if available and not forcing refresh
        if (!forceRefresh) {
            charactersCache?.let { return Result.Success(it) }
        }
        
        val result = dataSource.getCharacters()
        if (result is Result.Success) {
            val filtered = result.data.filter { it.id != "ID" }
            
            // Merge logic: preserve items/skills/weapons from heavy cache 
            // if the new data is 'light' (e.g. empty items list)
            val merged = filtered.map { newChar ->
                val heavy = heavyCharacterCache[newChar.id]
                if (heavy != null && newChar.items.isEmpty() && heavy.items.isNotEmpty()) {
                    newChar.copy(
                        items = heavy.items,
                        weapons = heavy.weapons,
                        skills = heavy.skills,
                        features = heavy.features,
                        proficiencies = heavy.proficiencies
                    )
                } else {
                    // If the new data actually has items, update the heavy cache
                    if (newChar.items.isNotEmpty()) {
                        heavyCharacterCache[newChar.id] = newChar
                    }
                    newChar
                }
            }
            
            charactersCache = merged
            return Result.Success(merged)
        }
        return result
    }

    override suspend fun getCharacter(id: String): Result<Character> {
        val result = dataSource.getCharacter(id)
        if (result is Result.Success) {
            val character = result.data
            // Update heavy cache with the fresh full detail
            heavyCharacterCache[character.id] = character
            // Also update list cache if it exists
            charactersCache = charactersCache?.map { if (it.id == character.id) character else it }
        }
        return result
    }

    override suspend fun saveCharacter(character: Character): Result<Unit> {
        val result = dataSource.saveCharacter(character)
        if (result is Result.Success) {
            // Update heavy cache
            heavyCharacterCache[character.id] = character
            // Optimistically update list cache
            charactersCache = charactersCache?.map { if (it.id == character.id) character else it }
        }
        return result
    }

    override suspend fun deleteCharacter(id: String): Result<Unit> {
        val result = dataSource.deleteCharacter(id)
        if (result is Result.Success) {
            charactersCache = charactersCache?.filter { it.id != id }
        }
        return result
    }

    override suspend fun getLocations(forceRefresh: Boolean): Result<List<Location>> {
        if (!forceRefresh) {
            locationsCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getLocations()
        if (result is Result.Success) locationsCache = result.data
        return result
    }

    override suspend fun saveLocation(location: Location): Result<Unit> {
        val result = dataSource.saveLocation(location)
        if (result is Result.Success) locationsCache = null // invalidate
        return result
    }

    override suspend fun deleteLocation(id: String): Result<Unit> {
        val result = dataSource.deleteLocation(id)
        if (result is Result.Success) locationsCache = null
        return result
    }

    override suspend fun getMonsters(forceRefresh: Boolean): Result<List<Monster>> {
        if (!forceRefresh) {
            monstersCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getMonsters()
        if (result is Result.Success) monstersCache = result.data
        return result
    }

    override suspend fun saveMonster(monster: Monster): Result<Unit> {
        val result = dataSource.saveMonster(monster)
        if (result is Result.Success) monstersCache = null
        return result
    }

    override suspend fun deleteMonster(id: String): Result<Unit> {
        val result = dataSource.deleteMonster(id)
        if (result is Result.Success) monstersCache = null
        return result
    }

    override suspend fun getNpcs(forceRefresh: Boolean): Result<List<Npc>> {
        if (!forceRefresh) {
            npcsCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getNpcs()
        if (result is Result.Success) npcsCache = result.data
        return result
    }

    override suspend fun saveNpc(npc: Npc): Result<Unit> {
        val result = dataSource.saveNpc(npc)
        if (result is Result.Success) npcsCache = null
        return result
    }

    override suspend fun deleteNpc(id: String): Result<Unit> {
        val result = dataSource.deleteNpc(id)
        if (result is Result.Success) npcsCache = null
        return result
    }

    override suspend fun getLogs(): Result<List<com.dnd.helper.domain.model.LogEntry>> {
        return dataSource.getLogs()
    }

    override suspend fun saveLog(log: com.dnd.helper.domain.model.LogEntry): Result<Unit> {
        return dataSource.saveLog(log)
    }

    override suspend fun getLastModified(): Result<String> {
        return dataSource.getLastModified()
    }
}
