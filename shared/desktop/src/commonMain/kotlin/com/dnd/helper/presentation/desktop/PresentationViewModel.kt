package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.model.PresentedItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class PresentationViewModel(
    private val repository: com.dnd.helper.domain.repository.CharacterRepository
) : ViewModel() {
    private val _isWindowOpen = MutableStateFlow(false)
    val isWindowOpen = _isWindowOpen.asStateFlow()

    private val _showStats = MutableStateFlow(false)
    val showStats = _showStats.asStateFlow()

    val activeItems = mutableStateListOf<PresentedItem>()

    // Assets for the sidebar
    private val _monsters = MutableStateFlow<List<com.dnd.helper.domain.model.Monster>>(emptyList())
    val monsters = _monsters.asStateFlow()

    private val _npcs = MutableStateFlow<List<com.dnd.helper.domain.model.Npc>>(emptyList())
    val npcs = _npcs.asStateFlow()

    private val _locations =
        MutableStateFlow<List<com.dnd.helper.domain.model.Location>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _battlefields =
        MutableStateFlow<List<com.dnd.helper.domain.model.Battlefield>>(emptyList())
    val battlefields = _battlefields.asStateFlow()

    private val _events = MutableStateFlow<List<com.dnd.helper.domain.model.GameEvent>>(emptyList())
    val events = _events.asStateFlow()

    private val _activeEvent = MutableStateFlow<com.dnd.helper.domain.model.GameEvent?>(null)
    val activeEvent = _activeEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val pendingSaveJobs = mutableMapOf<String, Job>()

    init {
        refreshAll()

        // Listen for remote updates via WebSocket
        viewModelScope.launch {
            repository.remoteUpdates.collect { updateMessage ->
                println("[Presentation] Remote update received via WebSocket: $updateMessage")
                val parts = updateMessage.split(":")
                val updateType = parts[0]
                val entityId = if (parts.size > 1) parts[1] else null

                if (entityId != null) {
                    // Selective refresh for specific entity
                    refreshSingleEntity(updateType, entityId)
                } else {
                    // Fallback to full refresh if no ID provided
                    if (updateType in listOf(
                            "characters",
                            "monsters",
                            "npcs",
                            "locations",
                            "battlefields",
                            "events"
                        )
                    ) {
                        refreshAll(force = true)
                    }
                }
            }
        }
    }

    private suspend fun refreshSingleEntity(type: String, id: String) {
        if (pendingSaveJobs.containsKey(id)) {
            println("[Presentation] Skipping remote update for $id (pending local save)")
            return
        }

        when (type) {
            "characters" -> {
                // 1. Refresh in sidebar list is handled by characterListViewModel if it uses same character source

                // 2. Refresh in workspace
                val result = repository.getCharacter(id)
                if (result is com.dnd.helper.domain.common.Result.Success) {
                    val char = result.data
                    activeItems.forEachIndexed { index, item ->
                        if (item.type.lowercase() == "character" && item.sourceId == id) {
                            activeItems[index] = item.copy(
                                currentHp = char.currentHp,
                                maxHp = char.maxHp,
                                armorClass = char.combat.armorClass,
                                stats = char.stats,
                                title = char.name,
                                imageUrl = char.displayImageUrl,
                                description = char.description
                            )
                        }
                    }
                }
            }

            "monsters" -> {
                // Refresh monsters list in sidebar
                val mResult = repository.getMonsters(forceRefresh = true)
                if (mResult is com.dnd.helper.domain.common.Result.Success) {
                    _monsters.value = mResult.data
                    val monster = mResult.data.find { it.id == id }

                    // Refresh in workspace
                    if (monster != null) {
                        activeItems.forEachIndexed { index, item ->
                            if (item.type.lowercase() == "monster" && item.sourceId == id) {
                                activeItems[index] = item.copy(
                                    currentHp = monster.currentHp,
                                    maxHp = monster.maxHp,
                                    armorClass = monster.armorClass,
                                    stats = monster.stats,
                                    title = monster.name,
                                    imageUrl = monster.displayImageUrl,
                                    description = monster.description,
                                    subInfo = "${monster.size} ${monster.type} · CR ${monster.challengeRating}"
                                )
                            }
                        }
                    }
                }
            }

            "npcs" -> {
                val nResult = repository.getNpcs(forceRefresh = true)
                if (nResult is com.dnd.helper.domain.common.Result.Success) {
                    _npcs.value = nResult.data
                    val npc = nResult.data.find { it.id == id }
                    if (npc != null) {
                        activeItems.forEachIndexed { index, item ->
                            if (item.type.lowercase() == "npc" && item.sourceId == id) {
                                activeItems[index] = item.copy(
                                    title = npc.name,
                                    imageUrl = npc.displayImageUrl,
                                    description = npc.description,
                                    subInfo = npc.background
                                )
                            }
                        }
                    }
                }
            }

            "locations" -> {
                val lResult = repository.getLocations(forceRefresh = true)
                if (lResult is com.dnd.helper.domain.common.Result.Success) {
                    _locations.value =
                        lResult.data
                }
            }

            "battlefields" -> {
                val bResult = repository.getBattlefields(forceRefresh = true)
                if (bResult is com.dnd.helper.domain.common.Result.Success) {
                    _battlefields.value =
                        bResult.data
                }
            }

            "events" -> {
                val eResult = repository.getEvents(forceRefresh = true)
                if (eResult is com.dnd.helper.domain.common.Result.Success) {
                    _events.value =
                        eResult.data
                }
            }
        }
    }

    fun refreshAll(force: Boolean = false) {
        viewModelScope.launch {
            if (!force) _isLoading.value = true

            val mResult = repository.getMonsters(forceRefresh = force)
            if (mResult is com.dnd.helper.domain.common.Result.Success) {
                _monsters.value =
                    mResult.data
            }

            val nResult = repository.getNpcs(forceRefresh = force)
            if (nResult is com.dnd.helper.domain.common.Result.Success) _npcs.value = nResult.data

            val lResult = repository.getLocations(forceRefresh = force)
            if (lResult is com.dnd.helper.domain.common.Result.Success) {
                _locations.value =
                    lResult.data
            }

            val bResult = repository.getBattlefields(forceRefresh = force)
            if (bResult is com.dnd.helper.domain.common.Result.Success) {
                _battlefields.value =
                    bResult.data
            }

            val eResult = repository.getEvents(forceRefresh = force)
            if (eResult is com.dnd.helper.domain.common.Result.Success) _events.value = eResult.data

            refreshActiveItems()
            _isLoading.value = false
        }
    }

    private suspend fun refreshActiveItems() {
        // Only refresh characters and monsters since they have dynamic stats
        val uniqueCharIds =
            activeItems.filter { it.type.lowercase() == "character" }.mapNotNull { it.sourceId }
                .distinct()
        val uniqueMonsterIds =
            activeItems.filter { it.type.lowercase() == "monster" }.mapNotNull { it.sourceId }
                .distinct()
        val uniqueNpcIds =
            activeItems.filter { it.type.lowercase() == "npc" }.mapNotNull { it.sourceId }
                .distinct()

        // 1. Refresh Characters
        uniqueCharIds.forEach { id ->
            val result = repository.getCharacter(id)
            if (result is com.dnd.helper.domain.common.Result.Success) {
                val char = result.data
                activeItems.forEachIndexed { index, item ->
                    if (item.type.lowercase() == "character" && item.sourceId == id) {
                        activeItems[index] = item.copy(
                            currentHp = char.currentHp,
                            maxHp = char.maxHp,
                            armorClass = char.combat.armorClass,
                            stats = char.stats,
                            title = char.name,
                            imageUrl = char.displayImageUrl,
                            description = char.description
                        )
                    }
                }
            }
        }

        // 2. Refresh Monsters
        if (uniqueMonsterIds.isNotEmpty()) {
            val result = repository.getMonsters(forceRefresh = true)
            if (result is com.dnd.helper.domain.common.Result.Success) {
                uniqueMonsterIds.forEach { id ->
                    val monster = result.data.find { it.id == id }
                    if (monster != null) {
                        activeItems.forEachIndexed { index, item ->
                            if (item.type.lowercase() == "monster" && item.sourceId == id) {
                                activeItems[index] = item.copy(
                                    currentHp = monster.currentHp,
                                    maxHp = monster.maxHp,
                                    armorClass = monster.armorClass,
                                    stats = monster.stats,
                                    title = monster.name,
                                    imageUrl = monster.displayImageUrl,
                                    description = monster.description,
                                    subInfo = "${monster.size} ${monster.type} · CR ${monster.challengeRating}"
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Refresh NPCs
        if (uniqueNpcIds.isNotEmpty()) {
            val result = repository.getNpcs(forceRefresh = true)
            if (result is com.dnd.helper.domain.common.Result.Success) {
                uniqueNpcIds.forEach { id ->
                    val npc = result.data.find { it.id == id }
                    if (npc != null) {
                        activeItems.forEachIndexed { index, item ->
                            if (item.type.lowercase() == "npc" && item.sourceId == id) {
                                activeItems[index] = item.copy(
                                    title = npc.name,
                                    imageUrl = npc.displayImageUrl,
                                    description = npc.description,
                                    subInfo = npc.background
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun toggleWindow() {
        _isWindowOpen.update { !it }
    }

    fun setWindowOpen(open: Boolean) {
        _isWindowOpen.value = open
    }

    fun toggleStats() {
        _showStats.update { !it }
    }

    fun addItem(
        title: String,
        type: String = "Item",
        imageUrl: String? = null,
        isBackground: Boolean = false,
        currentHp: Int? = null,
        maxHp: Int? = null,
        armorClass: Int? = null,
        stats: com.dnd.helper.domain.model.CharacterStats? = null,
        subInfo: String? = null,
        sourceId: String? = null,
        description: String? = null
    ) {
        val id = Random.nextLong().toString()
        if (isBackground) {
            activeItems.removeAll { it.isBackground }
        }

        // Backgrounds default to filling the 1000x1000 logical canvas
        val width = if (isBackground) 1000f else 220f
        val height = if (isBackground) 1000f else 240f

        activeItems.add(
            PresentedItem(
                id = id,
                sourceId = sourceId,
                title = title,
                type = type,
                imageUrl = imageUrl,
                isBackground = isBackground,
                width = width,
                height = height,
                x = if (isBackground) 0f else 100f,
                y = if (isBackground) 0f else 100f,
                currentHp = currentHp,
                maxHp = maxHp,
                armorClass = armorClass,
                stats = stats,
                subInfo = subInfo,
                description = description
            )
        )
    }

    fun updatePosition(id: String, x: Float, y: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            // Allow all items to move into "overscan" areas (the area outside the 1000x1000 logical center)
            // This is necessary to place tokens on maps that have been stretched to fill wide screens.
            activeItems[index] = activeItems[index].copy(
                x = x.coerceIn(-2000f, 2000f),
                y = y.coerceIn(-2000f, 2000f)
            )
        }
    }

    fun updateSize(id: String, width: Float, height: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            // Allow backgrounds to be much larger than the logical 1000x1000 canvas
            val maxSide = if (item.isBackground) 5000f else 1000f
            activeItems[index] = item.copy(
                width = width.coerceIn(10f, maxSide),
                height = height.coerceIn(10f, maxSide)
            )
        }
    }

    fun updateZoom(id: String, delta: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            activeItems[index] = item.copy(
                zoom = (item.zoom + delta).coerceIn(1.0f, 10.0f)
            )
        }
    }

    fun updateOffset(id: String, dx: Float, dy: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            // We coerce the offset to keep the image content within bounds as much as possible
            // 0.5f is a heuristic for max pan
            activeItems[index] = item.copy(
                offsetX = (item.offsetX + dx).coerceIn(-1f, 1f),
                offsetY = (item.offsetY + dy).coerceIn(-1f, 1f)
            )
        }
    }

    fun updateHp(id: String, delta: Int) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            val maxHp = item.maxHp ?: 10
            val newHp = ((item.currentHp ?: maxHp) + delta).coerceIn(0, maxHp)
            activeItems[index] = item.copy(currentHp = newHp)

            val sourceId = item.sourceId ?: return

            // Debounced save to server
            pendingSaveJobs[sourceId]?.cancel()
            pendingSaveJobs[sourceId] = viewModelScope.launch {
                delay(1000)
                when (item.type.lowercase()) {
                    "character" -> {
                        val result = repository.getCharacter(sourceId)
                        if (result is com.dnd.helper.domain.common.Result.Success) {
                            val updatedChar = result.data.copy(currentHp = newHp)
                            // Optimistic update for other local observers
                            repository.optimisticUpdate(updatedChar)
                            // Actual save
                            repository.saveCharacter(updatedChar)
                        }
                    }

                    "monster" -> {
                        val result = repository.getMonsters(forceRefresh = true)
                        if (result is com.dnd.helper.domain.common.Result.Success) {
                            val monster = result.data.find { it.id == sourceId }
                            if (monster != null) {
                                repository.saveMonster(monster.copy(currentHp = newHp))
                            }
                        }
                    }
                }
                pendingSaveJobs.remove(sourceId)
            }
        }
    }

    fun removeItem(id: String) {
        activeItems.removeAll { it.id == id }
    }

    fun clearItems() {
        activeItems.clear()
    }

    fun saveCurrentEvent(name: String, id: String? = null) {
        val targetId = id ?: _activeEvent.value?.id ?: Random.nextLong().toString()
        val event = com.dnd.helper.domain.model.GameEvent(
            id = targetId,
            name = name,
            items = activeItems.toList()
        )

        // Optimistic update
        _activeEvent.value = event
        _events.update { list ->
            val index = list.indexOfFirst { it.id == targetId }
            if (index != -1) {
                list.toMutableList().apply { set(index, event) }
            } else {
                list + event
            }
        }

        viewModelScope.launch {
            repository.saveEvent(event)
            // Refresh to ensure server sync, but UI already updated
            val result = repository.getEvents(forceRefresh = true)
            if (result is com.dnd.helper.domain.common.Result.Success) {
                _events.value = result.data
            }
        }
    }

    fun renameEvent(id: String, newName: String) {
        val event = _events.value.find { it.id == id } ?: return
        val updated = event.copy(name = newName)

        // Optimistic update
        if (_activeEvent.value?.id == id) {
            _activeEvent.value = updated
        }
        _events.update { list ->
            list.map { if (it.id == id) updated else it }
        }

        viewModelScope.launch {
            repository.saveEvent(updated)
        }
    }

    fun saveAsNewEvent(name: String) {
        val event = com.dnd.helper.domain.model.GameEvent(
            id = Random.nextLong().toString(),
            name = name,
            items = activeItems.toList()
        )
        _activeEvent.value = event
        _events.update { it + event }

        viewModelScope.launch {
            repository.saveEvent(event)
        }
    }

    fun loadEvent(event: com.dnd.helper.domain.model.GameEvent) {
        _activeEvent.value = event
        activeItems.clear()
        activeItems.addAll(event.items)
    }

    fun deleteEvent(id: String) {
        // Optimistic update
        if (_activeEvent.value?.id == id) {
            _activeEvent.value = null
        }
        _events.update { it.filter { event -> event.id != id } }

        viewModelScope.launch {
            repository.deleteEvent(id)
        }
    }

    fun createNewScene() {
        _activeEvent.value = null
        activeItems.clear()
    }
}
