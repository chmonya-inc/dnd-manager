package com.dnd.helper.data.repository

import com.dnd.helper.data.remote.RemoteDataSource
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
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class CharacterRepositoryImpl(
    private val dataSource: RemoteDataSource,
    private val storage: CharacterStorage,
) : CharacterRepository {

    // ponytail: all mutable state is declared before repositoryScope/init on purpose.
    // The init-block collectors run eagerly under an unconfined dispatcher, so they must not
    // touch fields that are still null. Declaring state first makes initialization order
    // correct under any dispatcher.
    private val _characterUpdates = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val characterUpdates: SharedFlow<String> = _characterUpdates.asSharedFlow()

    private val _npcUpdates = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val npcUpdates: SharedFlow<String> = _npcUpdates.asSharedFlow()

    private val _monsterUpdates = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val monsterUpdates: SharedFlow<String> = _monsterUpdates.asSharedFlow()

    private val _locationUpdates = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val locationUpdates: SharedFlow<String> = _locationUpdates.asSharedFlow()

    private val _battlefieldUpdates = MutableSharedFlow<String>(extraBufferCapacity = 1)
    override val battlefieldUpdates: SharedFlow<String> = _battlefieldUpdates.asSharedFlow()

    // Simple in-memory caches to make UI instant
    private var charactersCache: List<Character>? = null
    private val heavyCharacterCache = mutableMapOf<String, Character>()
    private var locationsCache: List<Location>? = null
    private var battlefieldsCache: List<Battlefield>? = null
    private var monstersCache: List<Monster>? = null
    private var npcsCache: List<Npc>? = null
    private var musicCache: List<MusicTrack>? = null
    private var eventsCache: List<GameEvent>? = null

    /** Tracks the last table ID we fetched from; used to auto-invalidate caches on session switch. */
    private var lastTableId: String? = null

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override val remoteUpdates: SharedFlow<String> = dataSource.observeUpdates()
        .shareIn(repositoryScope, SharingStarted.Eagerly, replay = 0)

    init {
        repositoryScope.launch {
            combine(
                storage.getServerAddressFlow(),
                storage.getTableIdFlow(),
                storage.getUserIdFlow()
            ) { _, _, _ -> }.collect {
                println("[CharacterRepository] Server, Table, or User changed, clearing caches")
                clearCaches()

                // Trigger refresh in all observing ViewModels
                _characterUpdates.tryEmit("all")
                _npcUpdates.tryEmit("all")
                _monsterUpdates.tryEmit("all")
                _locationUpdates.tryEmit("all")
                _battlefieldUpdates.tryEmit("all")
            }
        }

        repositoryScope.launch {
            remoteUpdates.collect { updateMessage ->
                val parts = updateMessage.split(":")
                val updateType = parts[0]
                val entityId = if (parts.size > 1) parts[1] else null

                println("[CharacterRepository] Remote update received: $updateMessage")

                when (updateType) {
                    "characters" -> {
                        charactersCache = null
                        if (entityId != null) heavyCharacterCache.remove(entityId)
                        _characterUpdates.tryEmit(entityId ?: "all")
                    }
                    "npcs" -> {
                        npcsCache = null
                        _npcUpdates.tryEmit(entityId ?: "all")
                    }
                    "monsters" -> {
                        monstersCache = null
                        _monsterUpdates.tryEmit(entityId ?: "all")
                    }
                    "locations" -> {
                        locationsCache = null
                        _locationUpdates.tryEmit(entityId ?: "all")
                    }
                    "battlefields" -> {
                        battlefieldsCache = null
                        _battlefieldUpdates.tryEmit(entityId ?: "all")
                    }
                }
            }
        }
    }

    private fun clearCaches() {
        charactersCache = null
        heavyCharacterCache.clear()
        locationsCache = null
        battlefieldsCache = null
        monstersCache = null
        npcsCache = null
        musicCache = null
        eventsCache = null
    }

    /** Checks if the stored table ID changed since last call. If so, wipes all caches. */
    private fun checkTableIdChanged(): Boolean {
        val current = storage.getTableId()
        return if (current != lastTableId) {
            lastTableId = current
            charactersCache = null
            heavyCharacterCache.clear()
            locationsCache = null
            battlefieldsCache = null
            monstersCache = null
            npcsCache = null
            true
        } else {
            false
        }
    }

    override suspend fun getInitialData(): Result<InitialData> {
        checkTableIdChanged()
        val result = dataSource.getInitialData()
        if (result is Result.Success) {
            val data = result.data

            // Populate all caches
            val filteredChars = data.characters.filter { it.id != "ID" }
            charactersCache = filteredChars
            locationsCache = data.locations
            battlefieldsCache = data.battlefields
            monstersCache = data.monsters
            npcsCache = data.npcs
            musicCache = data.music
            eventsCache = data.events

            // Update heavy character cache for any characters that have items or notes
            filteredChars.forEach { char ->
                if (char.items.isNotEmpty() || char.notes.isNotEmpty()) {
                    heavyCharacterCache[char.id] = char
                }
            }
        }
        return result
    }

    override suspend fun getCharacters(forceRefresh: Boolean): Result<List<Character>> {
        val tableChanged = checkTableIdChanged()
        if (!forceRefresh && !tableChanged) {
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
                        spells = heavy.spells,
                        features = heavy.features,
                        proficiencies = heavy.proficiencies,
                        notes = heavy.notes
                    )
                } else {
                    // If the new data actually has items or notes, update the heavy cache
                    if (newChar.items.isNotEmpty() || newChar.notes.isNotEmpty()) {
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
        checkTableIdChanged()
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

            // Update list cache: replace if exists, or append if new
            val currentList = charactersCache ?: emptyList()
            if (currentList.any { it.id == character.id }) {
                charactersCache = currentList.map { if (it.id == character.id) character else it }
            } else {
                charactersCache = currentList + character
            }

            // Notify observers so other screens can refresh immediately
            _characterUpdates.tryEmit(character.id)
        }
        return result
    }

    override fun optimisticUpdate(character: Character) {
        heavyCharacterCache[character.id] = character

        val currentList = charactersCache ?: emptyList()
        if (currentList.any { it.id == character.id }) {
            charactersCache = currentList.map { if (it.id == character.id) character else it }
        } else {
            charactersCache = currentList + character
        }

        _characterUpdates.tryEmit(character.id)
    }

    override suspend fun deleteCharacter(id: String): Result<Unit> {
        val result = dataSource.deleteCharacter(id)
        if (result is Result.Success) {
            charactersCache = charactersCache?.filter { it.id != id }
        }
        return result
    }

    override suspend fun getLocations(forceRefresh: Boolean): Result<List<Location>> {
        val tableChanged = checkTableIdChanged()
        if (!forceRefresh && !tableChanged) {
            locationsCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getLocations()
        if (result is Result.Success) locationsCache = result.data
        return result
    }

    override suspend fun saveLocation(location: Location): Result<Unit> {
        val result = dataSource.saveLocation(location)
        if (result is Result.Success) {
            locationsCache = null // invalidate
            _locationUpdates.tryEmit(location.id)
        }
        return result
    }

    override suspend fun deleteLocation(id: String): Result<Unit> {
        val result = dataSource.deleteLocation(id)
        if (result is Result.Success) locationsCache = null
        return result
    }

    override suspend fun getBattlefields(forceRefresh: Boolean): Result<List<Battlefield>> {
        val tableChanged = checkTableIdChanged()
        if (!forceRefresh && !tableChanged) {
            battlefieldsCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getBattlefields()
        if (result is Result.Success) battlefieldsCache = result.data
        return result
    }

    override suspend fun saveBattlefield(battlefield: Battlefield): Result<Unit> {
        val result = dataSource.saveBattlefield(battlefield)
        if (result is Result.Success) {
            battlefieldsCache = null
            _battlefieldUpdates.tryEmit(battlefield.id)
        }
        return result
    }

    override suspend fun deleteBattlefield(id: String): Result<Unit> {
        val result = dataSource.deleteBattlefield(id)
        if (result is Result.Success) battlefieldsCache = null
        return result
    }

    override suspend fun getMonsters(forceRefresh: Boolean): Result<List<Monster>> {
        val tableChanged = checkTableIdChanged()
        if (!forceRefresh && !tableChanged) {
            monstersCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getMonsters()
        if (result is Result.Success) monstersCache = result.data
        return result
    }

    override suspend fun saveMonster(monster: Monster): Result<Unit> {
        val result = dataSource.saveMonster(monster)
        if (result is Result.Success) {
            monstersCache = null
            _monsterUpdates.tryEmit(monster.id)
        }
        return result
    }

    override suspend fun deleteMonster(id: String): Result<Unit> {
        val result = dataSource.deleteMonster(id)
        if (result is Result.Success) monstersCache = null
        return result
    }

    override suspend fun getNpcs(forceRefresh: Boolean): Result<List<Npc>> {
        val tableChanged = checkTableIdChanged()
        if (!forceRefresh && !tableChanged) {
            npcsCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getNpcs()
        if (result is Result.Success) npcsCache = result.data
        return result
    }

    override suspend fun saveNpc(npc: Npc): Result<Unit> {
        val result = dataSource.saveNpc(npc)
        if (result is Result.Success) {
            npcsCache = null
            _npcUpdates.tryEmit(npc.id)
        }
        return result
    }

    override suspend fun deleteNpc(id: String): Result<Unit> {
        val result = dataSource.deleteNpc(id)
        if (result is Result.Success) npcsCache = null
        return result
    }

    override suspend fun getMusic(forceRefresh: Boolean): Result<List<MusicTrack>> {
        val tableChanged = checkTableIdChanged()
        if (!forceRefresh && !tableChanged) {
            musicCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getMusic()
        if (result is Result.Success) musicCache = result.data
        return result
    }

    override suspend fun saveMusic(music: MusicTrack): Result<Unit> {
        val result = dataSource.saveMusic(music)
        if (result is Result.Success) musicCache = null
        return result
    }

    override suspend fun deleteMusic(id: String): Result<Unit> {
        val result = dataSource.deleteMusic(id)
        if (result is Result.Success) musicCache = null
        return result
    }

    override suspend fun getLogs(): Result<List<LogEntry>> {
        return dataSource.getLogs()
    }

    override suspend fun saveLog(log: LogEntry): Result<Unit> {
        return dataSource.saveLog(log)
    }

    override suspend fun getEvents(forceRefresh: Boolean): Result<List<GameEvent>> {
        val tableChanged = checkTableIdChanged()
        if (!forceRefresh && !tableChanged) {
            eventsCache?.let { return Result.Success(it) }
        }
        val result = dataSource.getEvents()
        if (result is Result.Success) eventsCache = result.data
        return result
    }

    override suspend fun saveEvent(event: GameEvent): Result<Unit> {
        val result = dataSource.saveEvent(event)
        if (result is Result.Success) eventsCache = null
        return result
    }

    override suspend fun deleteEvent(id: String): Result<Unit> {
        val result = dataSource.deleteEvent(id)
        if (result is Result.Success) eventsCache = null
        return result
    }
}
