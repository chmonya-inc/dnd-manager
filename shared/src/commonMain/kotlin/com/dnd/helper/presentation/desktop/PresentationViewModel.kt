package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

import com.dnd.helper.domain.model.PresentedItem
import kotlinx.coroutines.*

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

    private val _locations = MutableStateFlow<List<com.dnd.helper.domain.model.Location>>(emptyList())
    val locations = _locations.asStateFlow()

    private val _events = MutableStateFlow<List<com.dnd.helper.domain.model.GameEvent>>(emptyList())
    val events = _events.asStateFlow()

    private val _activeEvent = MutableStateFlow<com.dnd.helper.domain.model.GameEvent?>(null)
    val activeEvent = _activeEvent.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var lastKnownTimestamp: String? = null
    private var pollingJob: kotlinx.coroutines.Job? = null

    fun startPolling(intervalMs: Long = 1_000L) {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(intervalMs)
                checkForUpdates()
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun refreshAll(force: Boolean = false) {
        viewModelScope.launch {
            if (!force) _isLoading.value = true
            
            val mResult = repository.getMonsters(forceRefresh = force)
            if (mResult is com.dnd.helper.domain.common.Result.Success) _monsters.value = mResult.data
            
            val nResult = repository.getNpcs(forceRefresh = force)
            if (nResult is com.dnd.helper.domain.common.Result.Success) _npcs.value = nResult.data
            
            val lResult = repository.getLocations(forceRefresh = force)
            if (lResult is com.dnd.helper.domain.common.Result.Success) _locations.value = lResult.data

            val eResult = repository.getEvents(forceRefresh = force)
            if (eResult is com.dnd.helper.domain.common.Result.Success) _events.value = eResult.data
            
            refreshActiveItems()
            _isLoading.value = false
        }
    }

    private suspend fun checkForUpdates() {
        when (val result = repository.getLastModified()) {
            is com.dnd.helper.domain.common.Result.Success -> {
                val serverTimestamp = result.data
                if (lastKnownTimestamp != null && lastKnownTimestamp != serverTimestamp) {
                    println("[Presentation] Data changed on server ($lastKnownTimestamp → $serverTimestamp), refreshing all...")
                    refreshAll(force = true)
                }
                lastKnownTimestamp = serverTimestamp
            }
            is com.dnd.helper.domain.common.Result.Error -> { }
        }
    }

    private suspend fun refreshActiveItems() {
        // Only refresh characters and monsters since they have dynamic stats
        activeItems.forEachIndexed { index, item ->
            val sourceId = item.sourceId ?: return@forEachIndexed
            when (item.type.lowercase()) {
                "character" -> {
                    val result = repository.getCharacter(sourceId)
                    if (result is com.dnd.helper.domain.common.Result.Success) {
                        val char = result.data
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
                "monster" -> {
                    val result = repository.getMonsters(forceRefresh = true)
                    if (result is com.dnd.helper.domain.common.Result.Success) {
                        val monster = result.data.find { it.id == sourceId }
                        if (monster != null) {
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
                "npc" -> {
                    val result = repository.getNpcs(forceRefresh = true)
                    if (result is com.dnd.helper.domain.common.Result.Success) {
                        val npc = result.data.find { it.id == sourceId }
                        if (npc != null) {
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
        
        activeItems.add(PresentedItem(
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
        ))
    }

    fun updatePosition(id: String, x: Float, y: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            activeItems[index] = item.copy(
                x = x.coerceIn(0f, 1000f), 
                y = y.coerceIn(0f, 1000f)
            )
        }
    }

    fun updateSize(id: String, width: Float, height: Float) {
        val index = activeItems.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = activeItems[index]
            activeItems[index] = item.copy(
                width = width.coerceIn(10f, 1000f),
                height = height.coerceIn(10f, 1000f)
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
            
            viewModelScope.launch {
                when (item.type.lowercase()) {
                    "character" -> {
                        val result = repository.getCharacter(sourceId)
                        if (result is com.dnd.helper.domain.common.Result.Success) {
                            repository.saveCharacter(result.data.copy(currentHp = newHp))
                        }
                    }
                    "monster" -> {
                        val result = repository.getMonsters()
                        if (result is com.dnd.helper.domain.common.Result.Success) {
                            val monster = result.data.find { it.id == sourceId }
                            if (monster != null) {
                                repository.saveMonster(monster.copy(currentHp = newHp))
                            }
                        }
                    }
                }
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
