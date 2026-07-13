package com.dnd.helper.fakes

import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Battlefield
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.GameEvent
import com.dnd.helper.domain.model.InitialData
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.model.LogEntry
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.domain.model.MusicTrack
import com.dnd.helper.domain.model.Npc
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Fake CharacterRepository for EditingRepository tests. Provides controllable
 * behavior for character-related operations used during image generation.
 */
class FakeCharacterRepository : CharacterRepository {

    private val _characterUpdates = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private val _npcUpdates = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private val _monsterUpdates = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private val _locationUpdates = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private val _battlefieldUpdates = MutableSharedFlow<String>(extraBufferCapacity = 64)
    private val _remoteUpdates = MutableSharedFlow<String>(extraBufferCapacity = 64)

    // Store entities by ID for retrieval
    private val characters = mutableMapOf<String, Character>()
    private val npcs = mutableMapOf<String, Npc>()
    private val monsters = mutableMapOf<String, Monster>()
    private val locations = mutableMapOf<String, Location>()
    private val battlefields = mutableMapOf<String, Battlefield>()

    var saveCharacterResult: Result<Unit> = Result.Success(Unit)
    var saveNpcResult: Result<Unit> = Result.Success(Unit)
    var saveMonsterResult: Result<Unit> = Result.Success(Unit)
    var saveLocationResult: Result<Unit> = Result.Success(Unit)
    var saveBattlefieldResult: Result<Unit> = Result.Success(Unit)
    var getCharacterResult: Result<Character> = Result.Error(AppError.NotFound)
    var deleteCharacterResult: Result<Unit> = Result.Success(Unit)
    var deleteCharacterCalls = 0

    var getCharactersResult: Result<List<Character>> = Result.Success(emptyList())
    var getCharactersCalls = 0
        private set

    var getLogsResult: Result<List<com.dnd.helper.domain.model.LogEntry>> = Result.Success(emptyList())
    var saveLogResult: Result<Unit> = Result.Success(Unit)
    var saveLogCalls = 0
        private set

    var getInitialDataResult: Result<InitialData> = Result.Success(
        InitialData(
            characters = emptyList(),
            locations = emptyList(),
            monsters = emptyList(),
            npcs = emptyList(),
            lastModified = ""
        )
    )
    var getInitialDataCalls = 0
        private set

    // Alias for backward compatibility
    var characterResult: Result<Character>
        get() = getCharacterResult
        set(value) { getCharacterResult = value }

    var saveCharacterCalls = 0
        private set

    var saveNpcCalls = 0
        private set

    var saveMonsterCalls = 0
        private set

    var saveLocationCalls = 0
        private set

    var saveBattlefieldCalls = 0
        private set

    // Track saved characters for testing
    val savedCharacters = mutableMapOf<String, Character>()

    override val characterUpdates: Flow<String> = _characterUpdates
    override val npcUpdates: Flow<String> = _npcUpdates
    override val monsterUpdates: Flow<String> = _monsterUpdates
    override val locationUpdates: Flow<String> = _locationUpdates
    override val battlefieldUpdates: Flow<String> = _battlefieldUpdates
    override val remoteUpdates: Flow<String> = _remoteUpdates

    // Helper method for tests to simulate remote updates
    fun emitCharacterUpdate(update: String) {
        _characterUpdates.tryEmit(update)
    }

    fun emitNpcUpdate(update: String) {
        _npcUpdates.tryEmit(update)
    }

    fun emitMonsterUpdate(update: String) {
        _monsterUpdates.tryEmit(update)
    }

    fun emitLocationUpdate(update: String) {
        _locationUpdates.tryEmit(update)
    }

    fun emitBattlefieldUpdate(update: String) {
        _battlefieldUpdates.tryEmit(update)
    }

    override suspend fun getInitialData(): Result<InitialData> {
        getInitialDataCalls++
        return getInitialDataResult
    }

    override suspend fun getCharacters(forceRefresh: Boolean): Result<List<Character>> {
        getCharactersCalls++
        return if (characters.isNotEmpty() && getCharactersResult is Result.Success && (getCharactersResult as Result.Success).data.isEmpty()) {
            Result.Success(characters.values.toList())
        } else {
            getCharactersResult
        }
    }

    override suspend fun getCharacter(id: String): Result<Character> {
        val character = characters[id]
        return if (character != null) {
            Result.Success(character)
        } else {
            getCharacterResult
        }
    }

    override suspend fun saveCharacter(character: Character): Result<Unit> {
        saveCharacterCalls++
        characters[character.id] = character
        savedCharacters[character.id] = character // Track for testing
        _characterUpdates.tryEmit(character.id)
        return saveCharacterResult
    }

    override suspend fun deleteCharacter(id: String): Result<Unit> {
        deleteCharacterCalls++
        characters.remove(id)
        savedCharacters.remove(id)
        return deleteCharacterResult
    }

    override suspend fun getLocations(forceRefresh: Boolean): Result<List<Location>> =
        Result.Success(locations.values.toList())

    override suspend fun saveLocation(location: Location): Result<Unit> {
        saveLocationCalls++
        locations[location.id] = location
        _locationUpdates.tryEmit(location.id)
        return saveLocationResult
    }

    override suspend fun deleteLocation(id: String): Result<Unit> {
        locations.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun getBattlefields(forceRefresh: Boolean): Result<List<Battlefield>> =
        Result.Success(battlefields.values.toList())

    override suspend fun saveBattlefield(battlefield: Battlefield): Result<Unit> {
        saveBattlefieldCalls++
        battlefields[battlefield.id] = battlefield
        _battlefieldUpdates.tryEmit(battlefield.id)
        return saveBattlefieldResult
    }

    override suspend fun deleteBattlefield(id: String): Result<Unit> {
        battlefields.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun getMonsters(forceRefresh: Boolean): Result<List<Monster>> =
        Result.Success(monsters.values.toList())

    override suspend fun saveMonster(monster: Monster): Result<Unit> {
        saveMonsterCalls++
        monsters[monster.id] = monster
        _monsterUpdates.tryEmit(monster.id)
        return saveMonsterResult
    }

    override suspend fun deleteMonster(id: String): Result<Unit> {
        monsters.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun getNpcs(forceRefresh: Boolean): Result<List<Npc>> =
        Result.Success(npcs.values.toList())

    override suspend fun saveNpc(npc: Npc): Result<Unit> {
        saveNpcCalls++
        npcs[npc.id] = npc
        _npcUpdates.tryEmit(npc.id)
        return saveNpcResult
    }

    override suspend fun deleteNpc(id: String): Result<Unit> {
        npcs.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun getMusic(forceRefresh: Boolean): Result<List<MusicTrack>> =
        Result.Success(emptyList())

    override suspend fun saveMusic(music: MusicTrack): Result<Unit> = Result.Success(Unit)

    override suspend fun deleteMusic(id: String): Result<Unit> = Result.Success(Unit)

    override suspend fun getLogs(): Result<List<LogEntry>> = getLogsResult

    override suspend fun saveLog(log: LogEntry): Result<Unit> {
        saveLogCalls++
        return saveLogResult
    }

    override suspend fun getEvents(forceRefresh: Boolean): Result<List<GameEvent>> =
        Result.Success(emptyList())

    override suspend fun saveEvent(event: GameEvent): Result<Unit> = Result.Success(Unit)

    override suspend fun deleteEvent(id: String): Result<Unit> = Result.Success(Unit)

    override fun optimisticUpdate(character: Character) {
        characters[character.id] = character
        _characterUpdates.tryEmit(character.id)
    }
}
