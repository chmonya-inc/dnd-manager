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

/** Minimal CharacterRepository fake. Tracks saves and exposes a writable remote-updates flow. */
class FakeCharacterRepository : CharacterRepository {

    val remoteUpdatesFlow = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val characters = mutableMapOf<String, Character>()
    val savedCharacters = mutableMapOf<String, Character>()
    var deletedCharacterIds = mutableListOf<String>()

    private val npcs = mutableMapOf<String, Npc>()
    private val monsters = mutableMapOf<String, Monster>()
    private val locations = mutableMapOf<String, Location>()
    private val battlefields = mutableMapOf<String, Battlefield>()
    private val events = mutableMapOf<String, GameEvent>()

    private val _characterUpdates = MutableSharedFlow<String>(extraBufferCapacity = 16)
    private val _npcUpdates = MutableSharedFlow<String>(extraBufferCapacity = 16)
    private val _monsterUpdates = MutableSharedFlow<String>(extraBufferCapacity = 16)
    private val _locationUpdates = MutableSharedFlow<String>(extraBufferCapacity = 16)
    private val _battlefieldUpdates = MutableSharedFlow<String>(extraBufferCapacity = 16)

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

    var deleteNpcCalls = 0
        private set
    var deleteMonsterCalls = 0
        private set
    var deleteLocationCalls = 0
        private set
    var deleteBattlefieldCalls = 0
        private set

    var getMonstersCalls = 0
        private set
    var getNpcsCalls = 0
        private set
    var getLocationsCalls = 0
        private set
    var getBattlefieldsCalls = 0
        private set
    var getMusicCalls = 0
        private set
    var saveMusicCalls = 0
        private set
    var getMusicResult: Result<List<MusicTrack>> = Result.Success(emptyList())
    var saveMusicResult: Result<Unit> = Result.Success(Unit)
    var getEventsResult: Result<List<GameEvent>> = Result.Success(emptyList())
    var saveEventResult: Result<Unit> = Result.Success(Unit)
    var getEventsCalls = 0
        private set
    var saveEventCalls = 0
        private set
    var getCharacterResult: Result<Character> = Result.Error(AppError.NotFound)
    var getCharacterCalls = 0
        private set

    var saveCharacterResult: Result<Unit> = Result.Success(Unit)
    var saveMonsterResult: Result<Unit> = Result.Success(Unit)
    var saveNpcResult: Result<Unit> = Result.Success(Unit)
    var saveLocationResult: Result<Unit> = Result.Success(Unit)
    var saveBattlefieldResult: Result<Unit> = Result.Success(Unit)
    var saveLogResult: Result<Unit> = Result.Success(Unit)
    var getLogsResult: Result<List<LogEntry>> = Result.Success(emptyList())
    var saveLogCalls = 0
        private set
    var getLogsCalls = 0
        private set
    var getCharactersCalls = 0
        private set

    var getMonstersResult: Result<List<Monster>> = Result.Success(emptyList())
    var getNpcsResult: Result<List<Npc>> = Result.Success(emptyList())
    var getLocationsResult: Result<List<Location>> = Result.Success(emptyList())
    var getBattlefieldsResult: Result<List<Battlefield>> = Result.Success(emptyList())
    var getCharactersResult: Result<List<Character>> = Result.Success(emptyList())

    override val characterUpdates: Flow<String> = _characterUpdates
    override val npcUpdates: Flow<String> = _npcUpdates
    override val monsterUpdates: Flow<String> = _monsterUpdates
    override val locationUpdates: Flow<String> = _locationUpdates
    override val battlefieldUpdates: Flow<String> = _battlefieldUpdates
    override val remoteUpdates: Flow<String> = remoteUpdatesFlow

    // Helper methods for tests to simulate remote updates
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

    fun emitRemoteUpdate(update: String) {
        remoteUpdatesFlow.tryEmit(update)
    }

    override suspend fun getInitialData(): Result<InitialData> = Result.Success(
        InitialData(
            characters = characters.values.toList(),
            locations = locations.values.toList(),
            monsters = monsters.values.toList(),
            npcs = npcs.values.toList(),
            lastModified = ""
        )
    )

    override suspend fun getCharacters(forceRefresh: Boolean): Result<List<Character>> {
        getCharactersCalls++
        return if (characters.isNotEmpty() && getCharactersResult is Result.Success && (getCharactersResult as Result.Success).data.isEmpty()) {
            Result.Success(characters.values.toList())
        } else {
            getCharactersResult
        }
    }

    override suspend fun getCharacter(id: String): Result<Character> {
        getCharacterCalls++
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
        savedCharacters[character.id] = character
        _characterUpdates.tryEmit(character.id)
        return saveCharacterResult
    }

    override suspend fun deleteCharacter(id: String): Result<Unit> {
        characters.remove(id)
        deletedCharacterIds.add(id)
        return Result.Success(Unit)
    }

    override suspend fun getLocations(forceRefresh: Boolean): Result<List<Location>> {
        getLocationsCalls++
        return if (locations.isNotEmpty() && getLocationsResult is Result.Success && (getLocationsResult as Result.Success).data.isEmpty()) {
            Result.Success(locations.values.toList())
        } else {
            getLocationsResult
        }
    }

    override suspend fun saveLocation(location: Location): Result<Unit> {
        saveLocationCalls++
        locations[location.id] = location
        return saveLocationResult
    }

    override suspend fun deleteLocation(id: String): Result<Unit> {
        deleteLocationCalls++
        locations.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun getBattlefields(forceRefresh: Boolean): Result<List<Battlefield>> {
        getBattlefieldsCalls++
        return if (battlefields.isNotEmpty() && getBattlefieldsResult is Result.Success && (getBattlefieldsResult as Result.Success).data.isEmpty()) {
            Result.Success(battlefields.values.toList())
        } else {
            getBattlefieldsResult
        }
    }

    override suspend fun saveBattlefield(battlefield: Battlefield): Result<Unit> {
        saveBattlefieldCalls++
        battlefields[battlefield.id] = battlefield
        return saveBattlefieldResult
    }

    override suspend fun deleteBattlefield(id: String): Result<Unit> {
        deleteBattlefieldCalls++
        battlefields.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun getMonsters(forceRefresh: Boolean): Result<List<Monster>> {
        getMonstersCalls++
        return if (monsters.isNotEmpty() && getMonstersResult is Result.Success && (getMonstersResult as Result.Success).data.isEmpty()) {
            Result.Success(monsters.values.toList())
        } else {
            getMonstersResult
        }
    }

    override suspend fun saveMonster(monster: Monster): Result<Unit> {
        saveMonsterCalls++
        monsters[monster.id] = monster
        return saveMonsterResult
    }

    override suspend fun deleteMonster(id: String): Result<Unit> {
        deleteMonsterCalls++
        monsters.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun getNpcs(forceRefresh: Boolean): Result<List<Npc>> {
        getNpcsCalls++
        return if (npcs.isNotEmpty() && getNpcsResult is Result.Success && (getNpcsResult as Result.Success).data.isEmpty()) {
            Result.Success(npcs.values.toList())
        } else {
            getNpcsResult
        }
    }

    override suspend fun saveNpc(npc: Npc): Result<Unit> {
        saveNpcCalls++
        npcs[npc.id] = npc
        return saveNpcResult
    }

    override suspend fun deleteNpc(id: String): Result<Unit> {
        deleteNpcCalls++
        npcs.remove(id)
        return Result.Success(Unit)
    }

    override suspend fun getMusic(forceRefresh: Boolean): Result<List<MusicTrack>> {
        getMusicCalls++
        return getMusicResult
    }

    override suspend fun saveMusic(music: MusicTrack): Result<Unit> {
        saveMusicCalls++
        return saveMusicResult
    }
    override suspend fun deleteMusic(id: String) = Result.Success(Unit)
    override suspend fun getLogs(): Result<List<LogEntry>> {
        getLogsCalls++
        return getLogsResult
    }

    override suspend fun saveLog(log: LogEntry): Result<Unit> {
        saveLogCalls++
        return saveLogResult
    }
    override suspend fun getEvents(forceRefresh: Boolean): Result<List<GameEvent>> {
        getEventsCalls++
        return if (events.isNotEmpty() && getEventsResult is Result.Success && (getEventsResult as Result.Success).data.isEmpty()) {
            Result.Success(events.values.toList())
        } else {
            getEventsResult
        }
    }

    override suspend fun saveEvent(event: GameEvent): Result<Unit> {
        saveEventCalls++
        events[event.id] = event
        return saveEventResult
    }
    override suspend fun deleteEvent(id: String) = Result.Success(Unit)

    override fun optimisticUpdate(character: Character) {
        characters[character.id] = character
    }
}
