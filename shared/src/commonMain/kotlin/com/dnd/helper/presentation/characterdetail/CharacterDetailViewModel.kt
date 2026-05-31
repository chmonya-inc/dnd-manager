package com.dnd.helper.presentation.characterdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val repository: CharacterRepository,
    private val characterId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterDetailState())
    val state: StateFlow<CharacterDetailState> = _state.asStateFlow()

    /** Tracks the server's last-modified timestamp to detect external changes. */
    private var lastKnownTimestamp: String? = null

    /** Active polling job; null when not polling. */
    private var pollingJob: Job? = null

    /**
     * Set to `true` right before a local save so the next auto-refresh cycle
     * knows this app caused the timestamp change and can skip the redundant reload.
     * Cleared after the skip happens or on save error.
     */
    private var hasPendingLocalChange: Boolean = false

    init {
        loadCharacter()
    }

    fun onEvent(event: CharacterDetailEvent) {
        when (event) {
            CharacterDetailEvent.Refresh -> loadCharacter()
            is CharacterDetailEvent.UpdateStat -> updateStat(event.statName, event.delta)
            is CharacterDetailEvent.UpdateHp -> updateHp(event.delta)
            is CharacterDetailEvent.UpdateMaxHp -> updateMaxHp(event.delta)
            is CharacterDetailEvent.UpdateLevel -> updateLevel(event.delta)
            CharacterDetailEvent.ToggleEdit -> {
                val state = _state.value
                if (state.isEditing) {
                    _state.value = state.copy(isEditing = false, editedCharacter = null)
                } else {
                    _state.value = state.copy(isEditing = true, editedCharacter = state.character)
                }
            }
            is CharacterDetailEvent.EditCharacter -> {
                _state.value = _state.value.copy(editedCharacter = event.character)
            }
            CharacterDetailEvent.SaveChanges -> saveChanges()
        }
    }

    /** Starts auto-refresh polling. Call from DisposableEffect/onResume. */
    fun startAutoRefresh(intervalMs: Long = 4_000L) {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(intervalMs)
                // Skip auto-refresh while the user is actively editing —
                // we don't want to overwrite their in-progress changes.
                if (!_state.value.isEditing) {
                    checkForUpdates()
                }
            }
        }
    }

    /** Stops auto-refresh polling. Call from DisposableEffect/onDispose/onPause. */
    fun stopAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun checkForUpdates() {
        when (val result = repository.getLastModified()) {
            is Result.Success -> {
                val serverTimestamp = result.data
                if (lastKnownTimestamp != null && lastKnownTimestamp != serverTimestamp) {
                    if (hasPendingLocalChange) {
                        println("[AutoRefresh] Timestamp changed but we have a pending local change — skipping reload.")
                        hasPendingLocalChange = false
                        // Deliberately do NOT update lastKnownTimestamp here.
                        // If an external change happened at the same time, the next
                        // poll will still see the timestamp difference and reload.
                    } else {
                        println("[AutoRefresh] Character data changed on server ($lastKnownTimestamp → $serverTimestamp), reloading...")
                        loadCharacter(fromAutoRefresh = true)
                    }
                } else {
                    // Timestamp matches — clear any stale flag so we don't skip
                    // a legitimate external change on the next cycle.
                    hasPendingLocalChange = false
                }
                lastKnownTimestamp = serverTimestamp
            }
            is Result.Error -> {
                println("[AutoRefresh] Polling error: ${result.error}")
            }
        }
    }

    private fun saveChanges() {
        val edited = _state.value.editedCharacter ?: return
        _state.value = _state.value.copy(isSaving = true)
        hasPendingLocalChange = true

        viewModelScope.launch {
            when (val result = repository.saveCharacter(edited)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        character = edited,
                        isEditing = false,
                        editedCharacter = null,
                        isSaving = false
                    )
                }
                is Result.Error -> {
                    hasPendingLocalChange = false
                    _state.value = _state.value.copy(
                        error = "Failed to save changes: ${result.error}",
                        isSaving = false
                    )
                }
            }
        }
    }

    private fun updateLevel(delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val updatedCharacter = currentCharacter.copy(
            level = (currentCharacter.level + delta).coerceAtLeast(1)
        )
        saveCharacter(updatedCharacter)
    }

    private fun updateStat(statName: String, delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val stats = currentCharacter.stats
        val newStats = when (statName.lowercase()) {
            "strength" -> stats.copy(strength = (stats.strength + delta).coerceAtLeast(1))
            "dexterity" -> stats.copy(dexterity = (stats.dexterity + delta).coerceAtLeast(1))
            "constitution" -> stats.copy(constitution = (stats.constitution + delta).coerceAtLeast(1))
            "intelligence" -> stats.copy(intelligence = (stats.intelligence + delta).coerceAtLeast(1))
            "wisdom" -> stats.copy(wisdom = (stats.wisdom + delta).coerceAtLeast(1))
            "charisma" -> stats.copy(charisma = (stats.charisma + delta).coerceAtLeast(1))
            else -> stats
        }
        val updatedCharacter = currentCharacter.copy(stats = newStats)
        saveCharacter(updatedCharacter)
    }

    private fun updateHp(delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val updatedCharacter = currentCharacter.copy(
            currentHp = (currentCharacter.currentHp + delta).coerceIn(0, currentCharacter.maxHp)
        )
        saveCharacter(updatedCharacter)
    }

    private fun updateMaxHp(delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val newMaxHp = (currentCharacter.maxHp + delta).coerceAtLeast(1)
        val updatedCharacter = currentCharacter.copy(
            maxHp = newMaxHp,
            currentHp = currentCharacter.currentHp.coerceAtMost(newMaxHp)
        )
        saveCharacter(updatedCharacter)
    }

    private fun saveCharacter(character: com.dnd.helper.domain.model.Character) {
        // Optimistic update
        val previousCharacter = _state.value.character
        _state.value = _state.value.copy(character = character)
        hasPendingLocalChange = true

        viewModelScope.launch {
            when (val result = repository.saveCharacter(character)) {
                is Result.Success -> {
                    // Success, state is already updated
                }
                is Result.Error -> {
                    hasPendingLocalChange = false
                    // Rollback on error
                    _state.value = _state.value.copy(
                        character = previousCharacter,
                        error = "Failed to update: ${result.error}"
                    )
                }
            }
        }
    }

    private fun loadCharacter(fromAutoRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!fromAutoRefresh) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }
            when (val result = repository.getCharacter(characterId)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        character = result.data,
                        isLoading = false,
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        error = result.error.toString(),
                        isLoading = false,
                    )
                }
            }
        }
    }
}
