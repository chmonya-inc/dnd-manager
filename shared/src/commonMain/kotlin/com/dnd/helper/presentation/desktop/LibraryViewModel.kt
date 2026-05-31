package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

data class LibraryState(
    val selectedType: LibraryType = LibraryType.Items,
    val locations: List<Location> = emptyList(),
    val monsters: List<Monster> = emptyList(),
    val npcs: List<Npc> = emptyList(),
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LibraryViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private var lastKnownTimestamp: String? = null
    private var pollingJob: Job? = null

    init {
        refreshData()
    }

    fun onTypeSelected(type: LibraryType) {
        _state.value = _state.value.copy(selectedType = type)
        refreshData()
    }

    fun refreshData(force: Boolean = false) {
        viewModelScope.launch {
            if (!force && _state.value.isLoading) return@launch
            
            _state.value = _state.value.copy(isLoading = !force) // Don't show loader on background force refresh
            
            when (_state.value.selectedType) {
                LibraryType.Locations -> {
                    val result = repository.getLocations(forceRefresh = force)
                    if (result is Result.Success) _state.value = _state.value.copy(locations = result.data, isLoading = false)
                }
                LibraryType.Mobs -> {
                    val result = repository.getMonsters(forceRefresh = force)
                    if (result is Result.Success) _state.value = _state.value.copy(monsters = result.data, isLoading = false)
                }
                LibraryType.Npcs -> {
                    val result = repository.getNpcs(forceRefresh = force)
                    if (result is Result.Success) _state.value = _state.value.copy(npcs = result.data, isLoading = false)
                }
                LibraryType.Items -> {
                    val result = repository.getCharacters(forceRefresh = force)
                    if (result is Result.Success) _state.value = _state.value.copy(characters = result.data, isLoading = false)
                }
            }
        }
    }

    fun startPolling(intervalMs: Long = 5_000L) {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(intervalMs)
                checkForUpdates()
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun checkForUpdates() {
        when (val result = repository.getLastModified()) {
            is Result.Success -> {
                val serverTimestamp = result.data
                if (lastKnownTimestamp != null && lastKnownTimestamp != serverTimestamp) {
                    refreshData(force = true)
                }
                lastKnownTimestamp = serverTimestamp
            }
            is Result.Error -> {}
        }
    }

    fun deleteMonster(id: String) {
        viewModelScope.launch {
            val monster = _state.value.monsters.find { it.id == id }
            repository.deleteMonster(id)
            _state.value = _state.value.copy(monsters = _state.value.monsters.filter { it.id != id })
            
            repository.saveLog(LogEntry(
                action = "Delete Monster: ${monster?.name ?: id}",
                details = "Monster deleted from library",
                success = true
            ))
        }
    }

    fun deleteNpc(id: String) {
        viewModelScope.launch {
            val npc = _state.value.npcs.find { it.id == id }
            repository.deleteNpc(id)
            _state.value = _state.value.copy(npcs = _state.value.npcs.filter { it.id != id })
            
            repository.saveLog(LogEntry(
                action = "Delete NPC: ${npc?.name ?: id}",
                details = "NPC deleted from library",
                success = true
            ))
        }
    }

    fun deleteLocation(id: String) {
        viewModelScope.launch {
            val location = _state.value.locations.find { it.id == id }
            repository.deleteLocation(id)
            _state.value = _state.value.copy(locations = _state.value.locations.filter { it.id != id })
            
            repository.saveLog(LogEntry(
                action = "Delete Location: ${location?.name ?: id}",
                details = "Location deleted from library",
                success = true
            ))
        }
    }
    
    fun deleteItem(characterId: String, itemId: String) {
        viewModelScope.launch {
            val char = _state.value.characters.find { it.id == characterId }
            if (char != null) {
                val item = char.items.find { it.id == itemId }
                val updatedChar = char.copy(items = char.items.filter { it.id != itemId })
                repository.saveCharacter(updatedChar)
                refreshData(force = true)
                
                repository.saveLog(LogEntry(
                    action = "Delete Item: ${item?.name ?: itemId}",
                    details = "Deleted from ${char.name}'s inventory",
                    initialState = Json.encodeToString(char),
                    endState = Json.encodeToString(updatedChar),
                    success = true
                ))
            }
        }
    }
    
    fun addItem(characterId: String, item: Item) {
        viewModelScope.launch {
            val char = _state.value.characters.find { it.id == characterId }
            if (char != null) {
                val updatedChar = char.copy(items = char.items + item)
                repository.saveCharacter(updatedChar)
                refreshData(force = true)
                
                repository.saveLog(LogEntry(
                    action = "Add Item: ${item.name}",
                    details = "Added to ${char.name}'s inventory",
                    initialState = Json.encodeToString(char),
                    endState = Json.encodeToString(updatedChar),
                    success = true
                ))
            }
        }
    }
}
