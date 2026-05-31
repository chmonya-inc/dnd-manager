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
    private var locationsCache: List<Location>? = null
    private var monstersCache: List<Monster>? = null
    private var npcsCache: List<Npc>? = null

    override suspend fun getCharacters(): Result<List<Character>> {
        // Return cached data immediately if available
        charactersCache?.let { return Result.Success(it) }
        
        val result = dataSource.getCharacters()
        if (result is Result.Success) {
            val filtered = result.data.filter { it.id != "ID" }
            charactersCache = filtered
            return Result.Success(filtered)
        }
        return result
    }

    override suspend fun getCharacter(id: String): Result<Character> {
        // We always fetch the full detail from network to be safe, 
        // but could cache this too if needed.
        return dataSource.getCharacter(id)
    }

    override suspend fun saveCharacter(character: Character): Result<Unit> {
        val result = dataSource.saveCharacter(character)
        if (result is Result.Success) {
            // Optimistically update cache
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

    override suspend fun getLocations(): Result<List<Location>> {
        locationsCache?.let { return Result.Success(it) }
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

    override suspend fun getMonsters(): Result<List<Monster>> {
        monstersCache?.let { return Result.Success(it) }
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

    override suspend fun getNpcs(): Result<List<Npc>> {
        npcsCache?.let { return Result.Success(it) }
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

    override suspend fun getLastModified(): Result<String> {
        return dataSource.getLastModified()
    }
}
