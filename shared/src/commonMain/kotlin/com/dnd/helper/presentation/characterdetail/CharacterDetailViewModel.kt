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

    /**
     * Active debounced save job. When the user rapidly edits stats/HP/level,
     * each click cancels the previous job and starts a new 5-second timer.
     * Only when the timer fires is the data pushed to the server.
     */
    private var debouncedSaveJob: Job? = null

    /**
     * The character state that needs to be synced to the server.
     * Updated on every optimistic local change. Flushed after 5s of inactivity.
     */
    private var pendingSaveCharacter: com.dnd.helper.domain.model.Character? = null

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
            is CharacterDetailEvent.ToggleItemEquipped -> toggleItemEquipped(event.itemId)
            CharacterDetailEvent.ToggleInspiration -> toggleInspiration()
            CharacterDetailEvent.RollDeathSave -> rollDeathSave()
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

    /**
     * Cancels any pending debounced save and immediately flushes to the server.
     * Call when navigating away from the screen or when the app goes to background
     * so that no local changes are lost.
     */
    fun flushPendingSave() {
        debouncedSaveJob?.cancel()
        debouncedSaveJob = null
        val character = pendingSaveCharacter
        if (character != null) {
            pendingSaveCharacter = null
            performSave(character)
        }
        _state.value = _state.value.copy(hasUnsavedChanges = false)
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
        // Edit-mode "Save" is an explicit user action — save immediately.
        performSave(edited)
        _state.value = _state.value.copy(isSaving = true)
    }

    private fun updateLevel(delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val updatedCharacter = currentCharacter.copy(
            level = (currentCharacter.level + delta).coerceAtLeast(1)
        )
        scheduleDebouncedSave(updatedCharacter)
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
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun updateHp(delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val newHp = (currentCharacter.currentHp + delta).coerceIn(0, currentCharacter.maxHp)
        val wasAtZero = currentCharacter.currentHp <= 0
        val isNowAtZero = newHp <= 0
        val resetDeathSaves = wasAtZero != isNowAtZero

        val updatedCombat = if (resetDeathSaves) {
            currentCharacter.combat.copy(
                deathSaveSuccesses = 0,
                deathSaveFailures = 0,
            )
        } else {
            currentCharacter.combat
        }

        val updatedCharacter = currentCharacter.copy(
            currentHp = newHp,
            combat = updatedCombat,
        )
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun updateMaxHp(delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val newMaxHp = (currentCharacter.maxHp + delta).coerceAtLeast(1)
        val updatedCharacter = currentCharacter.copy(
            maxHp = newMaxHp,
            currentHp = currentCharacter.currentHp.coerceAtMost(newMaxHp)
        )
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun toggleItemEquipped(itemId: String) {
        val currentCharacter = _state.value.character ?: return
        val updatedItems = currentCharacter.items.map { item ->
            if (item.id == itemId) item.copy(equipped = !item.equipped) else item
        }
        val updatedCharacter = currentCharacter.copy(items = updatedItems)
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun toggleInspiration() {
        val currentCharacter = _state.value.character ?: return
        val updatedCombat = currentCharacter.combat.copy(
            inspiration = !currentCharacter.combat.inspiration
        )
        val updatedCharacter = currentCharacter.copy(combat = updatedCombat)
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun rollDeathSave() {
        val currentCharacter = _state.value.character ?: return
        val combat = currentCharacter.combat
        if (combat.deathSaveSuccesses >= 3 || combat.deathSaveFailures >= 3) return

        val roll = (1..20).random()
        val isSuccess = roll >= 10
        val newSuccesses = (combat.deathSaveSuccesses + if (isSuccess) 1 else 0).coerceAtMost(3)
        val newFailures = (combat.deathSaveFailures + if (!isSuccess) 1 else 0).coerceAtMost(3)

        val updatedCombat = combat.copy(
            deathSaveSuccesses = newSuccesses,
            deathSaveFailures = newFailures
        )
        val updatedCharacter = currentCharacter.copy(combat = updatedCombat)
        scheduleDebouncedSave(updatedCharacter)
    }

    /**
     * Schedules a debounced save. Each new call cancels the previous timer.
     * After 5 seconds of inactivity the character is pushed to the server.
     * The UI is updated optimistically immediately.
     */
    private fun scheduleDebouncedSave(character: com.dnd.helper.domain.model.Character) {
        // Optimistic update — reflect change in UI immediately
        val previousCharacter = _state.value.character
        _state.value = _state.value.copy(
            character = character,
            hasUnsavedChanges = true,
        )
        pendingSaveCharacter = character

        // Cancel any existing debounce timer
        debouncedSaveJob?.cancel()

        debouncedSaveJob = viewModelScope.launch {
            delay(5_000L)
            // Timer fired — user hasn't made another change in 5s
            pendingSaveCharacter = null
            performSave(character)
        }
    }

    /**
     * Performs the actual network save. Used by both immediate saves (edit mode)
     * and debounced saves (stat/HP/level clicks).
     */
    private fun performSave(character: com.dnd.helper.domain.model.Character) {
        val previousCharacter = _state.value.character
        hasPendingLocalChange = true

        viewModelScope.launch {
            when (val result = repository.saveCharacter(character)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        hasUnsavedChanges = false,
                    )
                }
                is Result.Error -> {
                    hasPendingLocalChange = false
                    // Rollback on error
                    _state.value = _state.value.copy(
                        character = previousCharacter,
                        error = "Failed to update: ${result.error}",
                        isSaving = false,
                        hasUnsavedChanges = false,
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
