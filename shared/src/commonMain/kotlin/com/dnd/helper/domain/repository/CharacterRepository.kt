package com.dnd.helper.domain.repository

import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.*
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    /**
     * Emits the ID of a character whenever it is successfully saved locally.
     */
    val characterUpdates: Flow<String>

    /**
     * Emits the type of update (e.g., "characters", "monsters") whenever the server notifies via WebSocket.
     */
    val remoteUpdates: Flow<String>

    suspend fun getInitialData(): Result<InitialData>
    suspend fun getCharacters(forceRefresh: Boolean = false): Result<List<Character>>
    suspend fun getCharacter(id: String): Result<Character>
    suspend fun saveCharacter(character: Character): Result<Unit>
    suspend fun deleteCharacter(id: String): Result<Unit>

    suspend fun getLocations(forceRefresh: Boolean = false): Result<List<Location>>
    suspend fun saveLocation(location: Location): Result<Unit>
    suspend fun deleteLocation(id: String): Result<Unit>

    suspend fun getMonsters(forceRefresh: Boolean = false): Result<List<Monster>>
    suspend fun saveMonster(monster: Monster): Result<Unit>
    suspend fun deleteMonster(id: String): Result<Unit>

    suspend fun getNpcs(forceRefresh: Boolean = false): Result<List<Npc>>
    suspend fun saveNpc(npc: Npc): Result<Unit>
    suspend fun deleteNpc(id: String): Result<Unit>

    suspend fun getMusic(forceRefresh: Boolean = false): Result<List<MusicTrack>>
    suspend fun saveMusic(music: MusicTrack): Result<Unit>
    suspend fun deleteMusic(id: String): Result<Unit>

    suspend fun getLogs(): Result<List<LogEntry>>
    suspend fun saveLog(log: LogEntry): Result<Unit>

    suspend fun getEvents(forceRefresh: Boolean = false): Result<List<GameEvent>>
    suspend fun saveEvent(event: GameEvent): Result<Unit>
    suspend fun deleteEvent(id: String): Result<Unit>

    /**
     * Enqueues an image generation task in a process-scoped scope.
     * When the image is ready, it will automatically update the entity (character, npc, etc.) in the repository.
     */
    fun enqueueImageGeneration(prompt: String, entityId: String, entityType: String, genType: com.dnd.helper.data.remote.GenerationType = com.dnd.helper.data.remote.GenerationType.CHARACTER)

    /**
     * Updates the in-memory cache with the given character and notifies observers.
     * Use this for optimistic UI updates.
     */
    fun optimisticUpdate(character: Character)
}
