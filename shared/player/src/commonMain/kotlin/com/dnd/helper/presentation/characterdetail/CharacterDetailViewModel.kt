package com.dnd.helper.presentation.characterdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.data.remote.PromptGenerator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class CharacterDetailViewModel(
    private val repository: CharacterRepository,
    private val editingRepository: com.dnd.helper.domain.repository.EditingRepository,
    private val characterId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterDetailState())
    val state: StateFlow<CharacterDetailState> = _state.asStateFlow()

    /**
     * Active debounced save job. When the user rapidly edits stats/HP/level,
     * each click cancels the previous job and starts a new 1-second timer.
     * Only when the timer fires is the data pushed to the server.
     */
    private var debouncedSaveJob: Job? = null

    /**
     * Set to true for 1 second after a successful save to prevent
     * auto-refresh from immediately reloading data we just pushed.
     */
    private var isRecentlySaved = false

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

        // Listen for background image generation completion
        viewModelScope.launch {
            editingRepository.activeTasks.collect { tasks ->
                val myTasks = tasks.filter { 
                    it.entityId == characterId || it.entityId.startsWith("$characterId:") 
                }
                
                myTasks.forEach { task ->
                    // Handle completion or failure
                    if ((task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED && task.resultUrl != null) || 
                         task.status == com.dnd.helper.domain.repository.GenerationStatus.FAILED) {
                        
                        val resultUrl = if (task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED) task.resultUrl else ""
                        
                        if (task.entityType == "character") {
                            _state.update { currentState ->
                                var nextState = currentState
                                // Update main character
                                if (currentState.character?.imageUrl == "generating:${task.id}") {
                                    val updated = currentState.character.copy(imageUrl = resultUrl)
                                    nextState = nextState.copy(character = updated)
                                }
                                // Update edited character
                                if (currentState.editedCharacter?.imageUrl == "generating:${task.id}") {
                                    nextState = nextState.copy(editedCharacter = currentState.editedCharacter.copy(imageUrl = resultUrl))
                                }
                                nextState
                            }
                        } else if (task.entityType == "item") {
                            val itemId = task.entityId.substringAfter(":")
                            
                            _state.update { currentState ->
                                var nextState = currentState
                                // Update in main character
                                currentState.character?.let { char ->
                                    if (char.items.any { it.id == itemId && it.imageUrl == "generating:${task.id}" }) {
                                        val newItems = char.items.map { if (it.id == itemId) it.copy(imageUrl = resultUrl) else it }
                                        val updated = char.copy(items = newItems)
                                        nextState = nextState.copy(character = updated)
                                    }
                                }
                                // Update in edited character
                                currentState.editedCharacter?.let { char ->
                                    if (char.items.any { it.id == itemId && it.imageUrl == "generating:${task.id}" }) {
                                        val newItems = char.items.map { if (it.id == itemId) it.copy(imageUrl = resultUrl) else it }
                                        nextState = nextState.copy(editedCharacter = char.copy(items = newItems))
                                    }
                                }
                                nextState
                            }
                        }
                    }
                }
            }
        }

        // Listen for remote updates via WebSocket
        viewModelScope.launch {
            repository.remoteUpdates.collect { updateMessage ->
                val parts = updateMessage.split(":")
                val updateType = parts[0]
                val entityId = if (parts.size > 1) parts[1] else null

                if (updateType == "characters") {
                    val state = _state.value
                    
                    // If we have a specific ID, only update if it matches our character
                    if (entityId != null && entityId != characterId) {
                        return@collect
                    }

                    // If we are editing, we don't want to fully reload (and lose changes)
                    // but we might want to pick up some remote changes?
                    // For now, only reload if NOT editing and NOT saving
                    if (!state.isEditing && !state.hasUnsavedChanges && !isRecentlySaved) {
                        println("[CharacterDetail] Remote update received via WebSocket for $characterId, reloading character...")
                        loadCharacter(fromAutoRefresh = true)
                    } else {
                        println("[CharacterDetail] Remote update received but skipped (editing/saving)")
                    }
                }
            }
        }
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
                    val char = state.character
                    val prompt = if (char != null) {
                        PromptGenerator.getFullPrompt("${char.name}, ${char.race} ${char.characterClass}. ${char.description}", GenerationType.CHARACTER)
                    } else ""
                    _state.value = state.copy(isEditing = true, editedCharacter = char, aiPrompt = prompt)
                }
            }
            is CharacterDetailEvent.EditCharacter -> {
                val char = event.character
                val prompt = PromptGenerator.getFullPrompt("${char.name}, ${char.alignment} ${char.subrace} ${char.race} ${char.characterClass} ${char.subclass}. Background: ${char.background}. Description: ${char.description}. Armor: ${char.proficiencies.armor.joinToString()}. Weapons: ${char.proficiencies.weapons.joinToString()}.", GenerationType.CHARACTER)
                _state.value = _state.value.copy(editedCharacter = char, aiPrompt = prompt)
            }
            is CharacterDetailEvent.UpdateAiPrompt -> {
                _state.value = _state.value.copy(aiPrompt = event.prompt)
            }
            is CharacterDetailEvent.UpdateAiSize -> {
                _state.value = _state.value.copy(aiWidth = event.width, aiHeight = event.height)
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
            CharacterDetailEvent.GenerateImage -> generateImage()
            is CharacterDetailEvent.GenerateItemImage -> generateItemImage(event.itemId)
        }
    }

    private fun generateImage() {
        val currentState = _state.value
        val edited = currentState.editedCharacter ?: return
        val prompt = if (currentState.aiPrompt.isNotBlank()) currentState.aiPrompt else {
            val text = "${edited.name}, ${edited.race} ${edited.characterClass}. ${edited.description}"
            PromptGenerator.getFullPrompt(text, GenerationType.CHARACTER)
        }
        
        val mockUrl = editingRepository.startGeneration(
            entityId = characterId,
            entityType = "character",
            prompt = prompt,
            genType = GenerationType.CHARACTER,
            width = currentState.aiWidth,
            height = currentState.aiHeight
        )
        
        _state.value = currentState.copy(
            editedCharacter = edited.copy(imageUrl = mockUrl)
        )
        
        // If not in manual editing mode, update the character immediately so the list shows the mock URL
        if (!currentState.isEditing) {
            repository.optimisticUpdate(edited.copy(imageUrl = mockUrl))
        }
    }

    private fun generateItemImage(itemId: String) {
        val currentState = _state.value
        val edited = currentState.editedCharacter ?: return
        val item = edited.items.find { it.id == itemId } ?: return
        val promptText = "${item.name}, ${item.rarity}. ${item.description}"
        if (item.name.isBlank()) return

        val fullPrompt = PromptGenerator.getFullPrompt(promptText, GenerationType.ITEM)
        
        val mockUrl = editingRepository.startGeneration(
            entityId = "$characterId:$itemId",
            entityType = "item",
            prompt = fullPrompt,
            genType = GenerationType.ITEM
        )

        val newItems = edited.items.map {
            if (it.id == itemId) it.copy(imageUrl = mockUrl) else it
        }
        val updatedChar = edited.copy(items = newItems)
        _state.value = currentState.copy(editedCharacter = updatedChar)

        if (!currentState.isEditing) {
            repository.optimisticUpdate(updatedChar)
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

    /** No-op for WebSocket version */
    fun startAutoRefresh(intervalMs: Long = 1_000L) {
    }

    /** No-op for WebSocket version */
    fun stopAutoRefresh() {
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
                        character = character,
                        isSaving = false,
                        hasUnsavedChanges = false,
                        isEditing = false
                    )
                    
                    // Pause polling (skip remote updates) for 1 second after a successful save
                    isRecentlySaved = true
                    viewModelScope.launch {
                        delay(1000)
                        isRecentlySaved = false
                    }
                    
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
                    if (!fromAutoRefresh) {
                        _state.value = _state.value.copy(
                            error = result.error.toString(),
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }
}
