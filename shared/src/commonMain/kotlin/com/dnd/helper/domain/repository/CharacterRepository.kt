package com.dnd.helper.domain.repository

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.Location
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    /**
     * Emits the ID of a character whenever it is successfully saved.
     * Useful for cross-screen immediate refresh.
     */
    val characterUpdates: Flow<String>

    suspend fun getInitialData(): Result<com.dnd.helper.domain.model.InitialData>
    suspend fun getCharacters(forceRefresh: Boolean = false): Result<List<Character>>
    suspend fun getCharacter(id: String): Result<Character>
    suspend fun saveCharacter(character: Character): Result<Unit>
    suspend fun deleteCharacter(id: String): Result<Unit>

    suspend fun getLocations(forceRefresh: Boolean = false): Result<List<Location>>
    suspend fun saveLocation(location: Location): Result<Unit>
    suspend fun deleteLocation(id: String): Result<Unit>

    suspend fun getMonsters(forceRefresh: Boolean = false): Result<List<com.dnd.helper.domain.model.Monster>>
    suspend fun saveMonster(monster: com.dnd.helper.domain.model.Monster): Result<Unit>
    suspend fun deleteMonster(id: String): Result<Unit>

    suspend fun getNpcs(forceRefresh: Boolean = false): Result<List<com.dnd.helper.domain.model.Npc>>
    suspend fun saveNpc(npc: com.dnd.helper.domain.model.Npc): Result<Unit>
    suspend fun deleteNpc(id: String): Result<Unit>

    suspend fun getLogs(): Result<List<com.dnd.helper.domain.model.LogEntry>>
    suspend fun saveLog(log: com.dnd.helper.domain.model.LogEntry): Result<Unit>

    /**
     * Returns the server's last-modified timestamp (ISO-8601 string).
     * Lightweight — used for auto-refresh polling.
     */
    suspend fun getLastModified(): Result<String>

    /**
     * Updates the in-memory cache with the given character and notifies observers.
     * Use this for optimistic UI updates that should be visible immediately
     * across screens before the server save completes.
     */
    fun optimisticUpdate(character: Character)
}
