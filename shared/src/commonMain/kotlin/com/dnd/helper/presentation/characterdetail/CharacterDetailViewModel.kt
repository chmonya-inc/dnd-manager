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
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

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
     * each click cancels the previous job and starts a new 1-second timer.
     * Only when the timer fires is the data pushed to the server.
     */
    private var debouncedSaveJob: Job? = null

    /**
     * The character state as it exists on the server.
     * Updated only after a successful save or a full load/reload.
     */
    private var originalCharacter: com.dnd.helper.domain.model.Character? = null

    /**
     * The character state that needs to be synced to the server.
     * Updated on every optimistic local change. Flushed after 1s of inactivity.
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
            CharacterDetailEvent.ToggleMasterMode -> {
                _state.value = _state.value.copy(isMasterMode = !_state.value.isMasterMode)
            }
            CharacterDetailEvent.AddSkill -> addSkill()
            is CharacterDetailEvent.RemoveSkill -> removeSkill(event.skillId)
            is CharacterDetailEvent.UpdateSkill -> updateSkill(event.skill)
            CharacterDetailEvent.AddItem -> addItem()
            is CharacterDetailEvent.RemoveItem -> removeItem(event.itemId)
            is CharacterDetailEvent.UpdateItem -> updateItem(event.item)
            CharacterDetailEvent.AddNote -> addNote()
            is CharacterDetailEvent.RemoveNote -> removeNote(event.noteId)
            is CharacterDetailEvent.UpdateNote -> updateNote(event.note)
        }
    }

    private fun addNote() {
        val current = _state.value.character ?: return
        val newNote = com.dnd.helper.domain.model.Note(
            id = "note-${kotlin.random.Random.nextInt()}",
            title = "New Note",
            content = "",
            timestamp = 0L // For simplicity in common code
        )
        val updatedCharacter = current.copy(notes = current.notes + newNote)
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun removeNote(noteId: String) {
        val current = _state.value.character ?: return
        val updatedCharacter = current.copy(notes = current.notes.filter { it.id != noteId })
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun updateNote(note: com.dnd.helper.domain.model.Note) {
        val current = _state.value.character ?: return
        val updatedNotes = current.notes.map { if (it.id == note.id) note else it }
        val updatedCharacter = current.copy(notes = updatedNotes)
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun addItem() {
        val current = _state.value.character ?: return
        val newItem = com.dnd.helper.domain.model.Item(
            id = "item-${kotlin.random.Random.nextInt()}",
            name = "New Item",
            slot = null,
            rarity = com.dnd.helper.domain.model.ItemRarity.COMMON
        )
        val updatedCharacter = current.copy(items = current.items + newItem)
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun removeItem(itemId: String) {
        val current = _state.value.character ?: return
        val updatedCharacter = current.copy(items = current.items.filter { it.id != itemId })
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun addSkill() {
        val current = _state.value.character ?: return
        val newSkill = com.dnd.helper.domain.model.Skill(
            id = "skill-${kotlin.random.Random.nextInt()}",
            name = "New Skill",
            description = "Click edit to change details",
            level = 0
        )
        val updatedCharacter = current.copy(skills = current.skills + newSkill)
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun removeSkill(skillId: String) {
        val current = _state.value.character ?: return
        val updatedCharacter = current.copy(skills = current.skills.filter { it.id != skillId })
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun updateSkill(skill: com.dnd.helper.domain.model.Skill) {
        val current = _state.value.character ?: return
        val updatedSkills = current.skills.map { if (it.id == skill.id) skill else it }
        val updatedCharacter = current.copy(skills = updatedSkills)
        scheduleDebouncedSave(updatedCharacter)
    }

    private fun updateItem(item: com.dnd.helper.domain.model.Item) {
        val current = _state.value.character ?: return
        val updatedItems = current.items.map { if (it.id == item.id) item else it }
        val updatedCharacter = current.copy(items = updatedItems)
        scheduleDebouncedSave(updatedCharacter)
    }

    /** Starts auto-refresh polling. Call from DisposableEffect/onResume. */
    fun startAutoRefresh(intervalMs: Long = 1_000L) {
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
                    // Skip reload while the user has pending debounced changes or a save is in flight.
                    // This prevents the server from overwriting optimistic UI updates.
                    if (hasPendingLocalChange || _state.value.hasUnsavedChanges) {
                        println("[AutoRefresh] Timestamp changed but we have unsaved local changes — skipping reload.")
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
        val state = _state.value
        if (state.isEditing) {
            val edited = state.editedCharacter ?: return
            
            // Use originalCharacter from before the edit started
            if (originalCharacter == null) {
                originalCharacter = state.character
            }

            // Edit-mode "Save" is an explicit user action — save immediately.
            performSave(edited)
        } else {
            // User explicitly clicked "Save" to flush pending debounced changes
            flushPendingSave()
        }
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
        _state.value = _state.value.copy(lastDeathSaveRoll = roll)
        scheduleDebouncedSave(updatedCharacter)
    }

    /**
     * Schedules a debounced save. Each new call cancels the previous timer.
     * After 1 second of inactivity the character is pushed to the server.
     * The UI is updated optimistically immediately.
     */
    private fun scheduleDebouncedSave(character: com.dnd.helper.domain.model.Character) {
        // Capture original state before the first edit in a sequence
        if (originalCharacter == null) {
            originalCharacter = _state.value.character
        }

        // Optimistic update — reflect change in UI immediately
        _state.value = _state.value.copy(
            character = character,
            hasUnsavedChanges = true,
        )
        pendingSaveCharacter = character

        // Push the optimistic change into the shared repository cache
        // so the character list (and any other observer) refreshes right away.
        repository.optimisticUpdate(character)

        // Cancel any existing debounce timer
        debouncedSaveJob?.cancel()

        debouncedSaveJob = viewModelScope.launch {
            delay(1_000L)
            // Timer fired — user hasn't made another change in 1s
            val characterToSave = pendingSaveCharacter ?: character
            pendingSaveCharacter = null
            performSave(characterToSave)
        }
    }

    /**
     * Performs the actual network save. Used by both immediate saves (edit mode)
     * and debounced saves (stat/HP/level clicks).
     */
    private fun performSave(character: com.dnd.helper.domain.model.Character) {
        val previousUICharacter = _state.value.character
        hasPendingLocalChange = true
        _state.value = _state.value.copy(isSaving = true)

        // For logs, use the state BEFORE any edits in this cycle
        val initialJson = originalCharacter?.let { Json.encodeToString(it) }
        val endJson = Json.encodeToString(character)
        
        // Calculate diff for human readable logs using original state
        val diffDetails = if (originalCharacter != null) {
            calculateCharacterDiff(originalCharacter!!, character)
        } else "Initial create"

        viewModelScope.launch {
            when (val result = repository.saveCharacter(character)) {
                is Result.Success -> {
                    // Update state IMMEDIATELY after data is saved.
                    // Don't wait for the secondary 'saveLog' network call.
                    _state.value = _state.value.copy(
                        isSaving = false,
                        hasUnsavedChanges = false,
                    )
                    
                    // On success, this new state becomes the original state for future edits
                    originalCharacter = character

                    // Log the change in background
                    repository.saveLog(com.dnd.helper.domain.model.LogEntry(
                        action = "Update Character: ${character.name}",
                        details = diffDetails,
                        initialState = initialJson,
                        endState = endJson,
                        success = true
                    ))
                }
                is Result.Error -> {
                    hasPendingLocalChange = false
                    // Rollback UI on error
                    _state.value = _state.value.copy(
                        character = previousUICharacter,
                        error = "Failed to update: ${result.error}",
                        isSaving = false,
                        hasUnsavedChanges = false,
                    )

                    repository.saveLog(com.dnd.helper.domain.model.LogEntry(
                        action = "Update Character Failed: ${character.name}",
                        details = diffDetails,
                        initialState = initialJson,
                        endState = endJson,
                        success = false
                    ))
                    
                    // Keep originalCharacter as is, so if user retries they have the right base
                }
            }
        }
    }

    private fun calculateCharacterDiff(old: com.dnd.helper.domain.model.Character, new: com.dnd.helper.domain.model.Character): String {
        val changes = mutableListOf<String>()
        
        // Stats
        if (old.stats.strength != new.stats.strength) changes.add("STR: ${old.stats.strength} -> ${new.stats.strength}")
        if (old.stats.dexterity != new.stats.dexterity) changes.add("DEX: ${old.stats.dexterity} -> ${new.stats.dexterity}")
        if (old.stats.constitution != new.stats.constitution) changes.add("CON: ${old.stats.constitution} -> ${new.stats.constitution}")
        if (old.stats.intelligence != new.stats.intelligence) changes.add("INT: ${old.stats.intelligence} -> ${new.stats.intelligence}")
        if (old.stats.wisdom != new.stats.wisdom) changes.add("WIS: ${old.stats.wisdom} -> ${new.stats.wisdom}")
        if (old.stats.charisma != new.stats.charisma) changes.add("CHA: ${old.stats.charisma} -> ${new.stats.charisma}")
        
        // Basic Info
        if (old.currentHp != new.currentHp) changes.add("HP: ${old.currentHp} -> ${new.currentHp}")
        if (old.maxHp != new.maxHp) changes.add("MaxHP: ${old.maxHp} -> ${new.maxHp}")
        if (old.level != new.level) changes.add("Level: ${old.level} -> ${new.level}")
        
        // Inventory
        if (old.items.size != new.items.size) {
            changes.add("Items: ${old.items.size} -> ${new.items.size}")
        } else {
            // Check for name changes or other property changes in existing items
            old.items.forEach { oldItem ->
                new.items.find { it.id == oldItem.id }?.let { newItem ->
                    if (oldItem.name != newItem.name) changes.add("Item Name: ${oldItem.name} -> ${newItem.name}")
                    if (oldItem.equipped != newItem.equipped) changes.add("${newItem.name}: ${if (newItem.equipped) "Equipped" else "Unequipped"}")
                }
            }
        }

        // Skills
        if (old.skills.size != new.skills.size) {
            changes.add("Skills: ${old.skills.size} -> ${new.skills.size}")
        } else {
            old.skills.forEach { oldSkill ->
                new.skills.find { it.id == oldSkill.id }?.let { newSkill ->
                    if (oldSkill.level != newSkill.level) changes.add("${newSkill.name} LVL: ${oldSkill.level} -> ${newSkill.level}")
                }
            }
        }

        // Notes
        if (old.notes.size != new.notes.size) {
            changes.add("Notes: ${old.notes.size} -> ${new.notes.size}")
        } else {
            old.notes.forEach { oldNote ->
                new.notes.find { it.id == oldNote.id }?.let { newNote ->
                    if (oldNote.title != newNote.title || oldNote.content != newNote.content) {
                        changes.add("Note '${newNote.title}' updated")
                    }
                }
            }
        }
        
        return if (changes.isEmpty()) "No significant changes" else changes.joinToString(", ")
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
                    originalCharacter = result.data
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
