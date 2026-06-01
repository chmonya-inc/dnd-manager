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
    
    /** 
     * Counter of active background saves. 
     * While > 0, we suppress polling reloads to avoid seeing partial/stale data.
     */
    private var pendingSaveCount = 0
    private val saveJobs = mutableMapOf<String, Job>()

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
                LibraryType.Items, LibraryType.Templates -> {
                    val result = repository.getCharacters(forceRefresh = force)
                    if (result is Result.Success) _state.value = _state.value.copy(characters = result.data, isLoading = false)
                }
            }
        }
    }

    fun startPolling(intervalMs: Long = 1_000L) {
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
                    if (pendingSaveCount > 0) {
                        println("[Library] Timestamp changed but we have $pendingSaveCount pending saves — skipping reload.")
                    } else {
                        refreshData(force = true)
                    }
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
        val currentState = _state.value
        val char = currentState.characters.find { it.id == characterId } ?: return
        
        // 1. Optimistic UI update
        val updatedChar = char.copy(items = char.items.filter { it.id != itemId })
        _state.value = currentState.copy(
            characters = currentState.characters.map { if (it.id == characterId) updatedChar else it }
        )

        // 2. Schedule server save
        scheduleCharacterSave(updatedChar, "Delete Item")
    }
    
    fun addItem(characterId: String, item: Item) {
        val currentState = _state.value
        val char = currentState.characters.find { it.id == characterId } ?: return
        
        // 1. Optimistic UI update
        val updatedChar = char.copy(items = char.items + item)
        _state.value = currentState.copy(
            characters = currentState.characters.map { if (it.id == characterId) updatedChar else it }
        )

        // 2. Schedule server save
        scheduleCharacterSave(updatedChar, "Add Item: ${item.name}")
    }

    fun moveItemBetweenCharacters(item: Item, fromCharId: String, toCharId: String) {
        if (fromCharId == toCharId) return

        val currentState = _state.value
        val fromChar = currentState.characters.find { it.id == fromCharId }
        val toChar = currentState.characters.find { it.id == toCharId }

        if (fromChar != null && toChar != null) {
            // 1. Optimistic UI update
            val newItem = item.copy(id = "item-${kotlin.random.Random.nextInt()}")
            val updatedFromChar = fromChar.copy(items = fromChar.items.filter { it.id != item.id })
            val updatedToChar = toChar.copy(items = toChar.items + newItem)

            _state.value = currentState.copy(
                characters = currentState.characters.map {
                    when (it.id) {
                        fromCharId -> updatedFromChar
                        toCharId -> updatedToChar
                        else -> it
                    }
                }
            )

            // 2. Schedule server saves (atomic pair)
            scheduleCharacterSave(updatedFromChar, "Move Item (Source): ${item.name}")
            scheduleCharacterSave(updatedToChar, "Move Item (Target): ${item.name}")
        }
    }

    private fun scheduleCharacterSave(character: Character, logAction: String) {
        // Cancel any existing save job for this specific character
        saveJobs[character.id]?.cancel()
        
        saveJobs[character.id] = viewModelScope.launch {
            pendingSaveCount++
            try {
                // Short debounce to aggregate rapid clicks (e.g. rapid deletes or moves)
                delay(300)
                
                // Always fetch the LATEST character state from our current UI state
                // because multiple operations might have happened while we were waiting/saving.
                val latestChar = _state.value.characters.find { it.id == character.id } ?: character
                
                repository.saveCharacter(latestChar)
                
                repository.saveLog(LogEntry(
                    action = logAction,
                    details = "Character ${latestChar.name} updated (Optimistic Save)",
                    success = true
                ))
            } catch (e: Exception) {
                println("[Library] Save failed for ${character.name}: $e")
            } finally {
                pendingSaveCount--
                saveJobs.remove(character.id)
            }
        }
    }
}
