package com.dnd.helper.presentation.characterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterListViewModel(
    private val repository: CharacterRepository,
    private val storage: CharacterStorage
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterListState())
    val state: StateFlow<CharacterListState> = _state.asStateFlow()

    /** Counter of active background saves. Suppresses polling reloads while > 0. */
    private var pendingSaveCount = 0
    private val saveJobs = mutableMapOf<String, Job>()

    init {
        loadInitialData()
        
        // Listen for local updates (immediate refresh for cross-screen changes)
        viewModelScope.launch {
            repository.characterUpdates.collect { updatedId ->
                println("[CharacterList] Received local update for $updatedId — reloading list")
                loadCharacters(fromAutoRefresh = true)
            }
        }

        // Listen for remote updates via WebSocket
        viewModelScope.launch {
            repository.remoteUpdates.collect { updateType ->
                if (updateType == "characters") {
                    if (pendingSaveCount > 0) {
                        println("[CharacterList] Remote update received but we have $pendingSaveCount pending saves — skipping reload.")
                    } else {
                        println("[CharacterList] Remote update received via WebSocket, reloading characters...")
                        loadCharacters(fromAutoRefresh = true)
                    }
                }
            }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.getInitialData()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        characters = result.data.characters,
                        isLoading = false,
                    )
                }
                is Result.Error -> {
                    // Fallback to separate loading if bulk loading fails
                    loadCharacters()
                }
            }
        }
    }

    fun onEvent(event: CharacterListEvent) {
        when (event) {
            CharacterListEvent.Refresh -> loadCharacters()
            is CharacterListEvent.CharacterClicked -> {
                storage.saveCharacterId(event.characterId)
            }
        }
    }

    fun moveItemBetweenCharacters(
        item: com.dnd.helper.domain.model.Item,
        fromCharId: String,
        toCharId: String
    ) {
        if (fromCharId == toCharId) return

        // 1. OPTIMISTIC UPDATE
        val currentState = _state.value
        val fromChar = currentState.characters.find { it.id == fromCharId }
        val toChar = currentState.characters.find { it.id == toCharId }

        if (fromChar != null && toChar != null) {
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

            // 2. Schedule server saves
            scheduleCharacterSave(updatedFromChar, "Move Item (Source): ${item.name}")
            scheduleCharacterSave(updatedToChar, "Move Item (Target): ${item.name}")
        }
    }

    private fun scheduleCharacterSave(character: com.dnd.helper.domain.model.Character, logAction: String) {
        saveJobs[character.id]?.cancel()
        saveJobs[character.id] = viewModelScope.launch {
            pendingSaveCount++
            try {
                delay(300)
                val latestChar = _state.value.characters.find { it.id == character.id } ?: character
                repository.saveCharacter(latestChar)
                repository.saveLog(com.dnd.helper.domain.model.LogEntry(
                    action = logAction,
                    details = "Character ${latestChar.name} updated (Optimistic List Save)",
                    success = true
                ))
            } catch (e: Exception) {
                println("[CharacterList] Save failed for ${character.name}: $e")
            } finally {
                pendingSaveCount--
                saveJobs.remove(character.id)
            }
        }
    }

    /** No-op for WebSocket version (kept for source compatibility if screens still call it) */
    fun startAutoRefresh(intervalMs: Long = 1_000L) {
        // WebSocket logic handles this in init
    }

    /** No-op for WebSocket version */
    fun stopAutoRefresh() {
        // WebSocket logic handles this via viewModelScope
    }

    private fun loadCharacters(fromAutoRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!fromAutoRefresh) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }
            when (val result = repository.getCharacters()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        characters = result.data,
                        isLoading = false,
                    )
                }
                is Result.Error -> {
                    if (!fromAutoRefresh) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }
}
