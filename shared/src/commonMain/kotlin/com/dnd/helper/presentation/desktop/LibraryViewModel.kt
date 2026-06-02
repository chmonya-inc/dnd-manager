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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.dnd.helper.data.remote.PromptGenerator
import com.dnd.helper.data.remote.GenerationType

data class LibraryState(
    val selectedType: LibraryType = LibraryType.Items,
    val locations: List<Location> = emptyList(),
    val battlefields: List<Battlefield> = emptyList(),
    val monsters: List<Monster> = emptyList(),
    val npcs: List<Npc> = emptyList(),
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LibraryViewModel(
    private val repository: CharacterRepository,
    private val editingRepository: com.dnd.helper.domain.repository.EditingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    /** 
     * Counter of active background saves. 
     * While > 0, we suppress polling reloads to avoid seeing partial/stale data.
     */
    private var pendingSaveCount = 0
    private val saveJobs = mutableMapOf<String, Job>()

    init {
        refreshData()

        // Listen for background image generation completion
        viewModelScope.launch {
            editingRepository.activeTasks.collect { tasks ->
                tasks.forEach { task ->
                    if ((task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED && task.resultUrl != null) || 
                         task.status == com.dnd.helper.domain.repository.GenerationStatus.FAILED) {
                        
                        val resultUrl = if (task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED) task.resultUrl else ""
                        
                        val currentState = _state.value
                        when (task.entityType) {
                            "npc" -> {
                                val npc = currentState.npcs.find { it.id == task.entityId }
                                if (npc != null) {
                                    val updatedNpc = npc.copy(imageUrl = resultUrl)
                                    _state.value = currentState.copy(npcs = currentState.npcs.map { if (it.id == npc.id) updatedNpc else it })
                                }
                            }
                            "monster" -> {
                                val monster = currentState.monsters.find { it.id == task.entityId }
                                if (monster != null) {
                                    val updatedMonster = monster.copy(imageUrl = resultUrl)
                                    _state.value = currentState.copy(monsters = currentState.monsters.map { if (it.id == monster.id) updatedMonster else it })
                                }
                            }
                            "location" -> {
                                val loc = currentState.locations.find { it.id == task.entityId }
                                if (loc != null) {
                                    val updatedLoc = loc.copy(imageUrl = resultUrl)
                                    _state.value = currentState.copy(locations = currentState.locations.map { if (it.id == loc.id) updatedLoc else it })
                                }
                            }
                            "battlefield" -> {
                                val bf = currentState.battlefields.find { it.id == task.entityId }
                                if (bf != null) {
                                    val updatedBf = bf.copy(imageUrl = resultUrl)
                                    _state.value = currentState.copy(battlefields = currentState.battlefields.map { if (it.id == bf.id) updatedBf else it })
                                }
                            }
                            "character" -> {
                                val char = currentState.characters.find { it.id == task.entityId }
                                if (char != null) {
                                    val updatedChar = char.copy(imageUrl = resultUrl)
                                    _state.value = currentState.copy(characters = currentState.characters.map { if (it.id == char.id) updatedChar else it })
                                }
                            }
                            "item" -> {
                                val parts = task.entityId.split(":")
                                if (parts.size == 2) {
                                    val charId = parts[0]
                                    val itemId = parts[1]
                                    val char = currentState.characters.find { it.id == charId }
                                    if (char != null && char.items.any { it.id == itemId }) {
                                        val updatedItems = char.items.map { if (it.id == itemId) it.copy(imageUrl = resultUrl) else it }
                                        val updatedChar = char.copy(items = updatedItems)
                                        _state.value = currentState.copy(characters = currentState.characters.map { if (it.id == charId) updatedChar else it })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Listen for remote updates via WebSocket
        viewModelScope.launch {
            repository.remoteUpdates.collect { updateType ->
                if (pendingSaveCount > 0) {
                    println("[Library] Remote update received but we have $pendingSaveCount pending saves — skipping reload.")
                    return@collect
                }

                val currentType = _state.value.selectedType
                val shouldReload = when (updateType) {
                    "characters" -> currentType == LibraryType.Items || currentType == LibraryType.Templates
                    "locations" -> currentType == LibraryType.Locations
                    "battlefields" -> currentType == LibraryType.Battlefields
                    "monsters" -> currentType == LibraryType.Mobs
                    "npcs" -> currentType == LibraryType.Npcs
                    else -> false
                }

                if (shouldReload) {
                    println("[Library] Remote update received via WebSocket for $updateType, reloading...")
                    refreshData(force = true)
                }
            }
        }

        // Listen for internal repository updates (e.g. from local saves)
        viewModelScope.launch {
            repository.npcUpdates.collect { refreshData(force = true) }
        }
        viewModelScope.launch {
            repository.monsterUpdates.collect { refreshData(force = true) }
        }
        viewModelScope.launch {
            repository.locationUpdates.collect { refreshData(force = true) }
        }
        viewModelScope.launch {
            repository.battlefieldUpdates.collect { refreshData(force = true) }
        }
        viewModelScope.launch {
            repository.characterUpdates.collect { refreshData(force = true) }
        }
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
                LibraryType.Battlefields -> {
                    val result = repository.getBattlefields(forceRefresh = force)
                    if (result is Result.Success) _state.value = _state.value.copy(battlefields = result.data, isLoading = false)
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

    /** No-op for WebSocket version */
    fun startPolling(intervalMs: Long = 1_000L) {
    }

    /** No-op for WebSocket version */
    fun stopPolling() {
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
    
    fun deleteBattlefield(id: String) {
        viewModelScope.launch {
            val battlefield = _state.value.battlefields.find { it.id == id }
            repository.deleteBattlefield(id)
            _state.value = _state.value.copy(battlefields = _state.value.battlefields.filter { it.id != id })
            
            repository.saveLog(LogEntry(
                action = "Delete Battlefield: ${battlefield?.name ?: id}",
                details = "Battlefield deleted from library",
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

    fun generateMissingImages(force: Boolean = false, customWidth: Int? = null, customHeight: Int? = null) {
        viewModelScope.launch {
            var generatedCount = 0
            val activeTaskIds = editingRepository.activeTasks.value.map { it.id }.toSet()

            val npcsResult = repository.getNpcs(forceRefresh = false)
            if (npcsResult is Result.Success) {
                npcsResult.data.forEach { npc ->
                    if (force || npc.imageUrl.needsImageGeneration(activeTaskIds)) {
                        val prompt = PromptGenerator.getFullPrompt("${npc.name}. ${npc.description}. ${npc.background}", GenerationType.NPC)
                        val mockUrl = editingRepository.startGeneration(npc.id, "npc", prompt, GenerationType.NPC, customWidth ?: 1024, customHeight ?: 1024)
                        val updatedNpc = npc.copy(imageUrl = mockUrl)
                        _state.update { it.copy(npcs = it.npcs.map { n -> if (n.id == npc.id) updatedNpc else n }) }
                        launch { repository.saveNpc(updatedNpc) }
                        generatedCount++
                    }
                }
            }

            val monstersResult = repository.getMonsters(forceRefresh = false)
            if (monstersResult is Result.Success) {
                monstersResult.data.forEach { monster ->
                    if (force || monster.imageUrl.needsImageGeneration(activeTaskIds)) {
                        val prompt = PromptGenerator.getFullPrompt("${monster.name}, ${monster.type}. ${monster.description}", GenerationType.MONSTER)
                        val mockUrl = editingRepository.startGeneration(monster.id, "monster", prompt, GenerationType.MONSTER, customWidth ?: 1024, customHeight ?: 1024)
                        val updatedMonster = monster.copy(imageUrl = mockUrl)
                        _state.update { it.copy(monsters = it.monsters.map { m -> if (m.id == monster.id) updatedMonster else m }) }
                        launch { repository.saveMonster(updatedMonster) }
                        generatedCount++
                    }
                }
            }

            val locationsResult = repository.getLocations(forceRefresh = false)
            if (locationsResult is Result.Success) {
                locationsResult.data.forEach { loc ->
                    if (force || loc.imageUrl.needsImageGeneration(activeTaskIds)) {
                        val prompt = PromptGenerator.getFullPrompt("${loc.name}. ${loc.description}", GenerationType.LOCATION)
                        val mockUrl = editingRepository.startGeneration(loc.id, "location", prompt, GenerationType.LOCATION, customWidth ?: 2048, customHeight ?: 2048)
                        val updatedLoc = loc.copy(imageUrl = mockUrl)
                        _state.update { it.copy(locations = it.locations.map { l -> if (l.id == loc.id) updatedLoc else l }) }
                        launch { repository.saveLocation(updatedLoc) }
                        generatedCount++
                    }
                }
            }

            val battlefieldsResult = repository.getBattlefields(forceRefresh = false)
            if (battlefieldsResult is Result.Success) {
                battlefieldsResult.data.forEach { bf ->
                    if (force || bf.imageUrl.needsImageGeneration(activeTaskIds)) {
                        val prompt = PromptGenerator.getFullPrompt("${bf.name}. ${bf.description}", GenerationType.BATTLEFIELD)
                        val mockUrl = editingRepository.startGeneration(bf.id, "battlefield", prompt, GenerationType.BATTLEFIELD, customWidth ?: 2048, customHeight ?: 2048)
                        val updatedBf = bf.copy(imageUrl = mockUrl)
                        _state.update { it.copy(battlefields = it.battlefields.map { b -> if (b.id == bf.id) updatedBf else b }) }
                        launch { repository.saveBattlefield(updatedBf) }
                        generatedCount++
                    }
                }
            }

            val charactersResult = repository.getCharacters(forceRefresh = false)
            if (charactersResult is Result.Success) {
                charactersResult.data.forEach { char ->
                    var charModified = false
                    var newCharImageUrl = char.imageUrl

                    if (force || char.imageUrl.needsImageGeneration(activeTaskIds)) {
                        val prompt = PromptGenerator.getFullPrompt("${char.name}, ${char.race} ${char.characterClass}. ${char.description}", GenerationType.CHARACTER)
                        newCharImageUrl = editingRepository.startGeneration(char.id, "character", prompt, GenerationType.CHARACTER, customWidth ?: 1024, customHeight ?: 1024)
                        charModified = true
                        generatedCount++
                    }

                    val updatedItems = char.items.map { item ->
                        if ((force || item.imageUrl.needsImageGeneration(activeTaskIds)) && item.name.isNotBlank()) {
                            val prompt = PromptGenerator.getFullPrompt("${item.name}, ${item.rarity}. ${item.description}", GenerationType.ITEM)
                            val mockUrl = editingRepository.startGeneration("${char.id}:${item.id}", "item", prompt, GenerationType.ITEM, customWidth ?: 1024, customHeight ?: 1024)
                            charModified = true
                            generatedCount++
                            item.copy(imageUrl = mockUrl)
                        } else item
                    }

                    if (charModified) {
                        val updatedChar = char.copy(imageUrl = newCharImageUrl, items = updatedItems)
                        _state.update { it.copy(characters = it.characters.map { c -> if (c.id == char.id) updatedChar else c }) }
                        launch { scheduleCharacterSave(updatedChar, "Generate Missing Images") }
                    }
                }
            }

            repository.saveLog(LogEntry(
                action = "Generate Missing Images",
                details = if (generatedCount > 0) "Started generating $generatedCount missing images in background." else "No missing images found.",
                success = true
            ))

            refreshData(force = true)
        }
    }

    private fun String?.needsImageGeneration(activeTaskIds: Set<String>): Boolean {
        if (this == null) return true
        if (this.startsWith("generating:")) {
            val taskId = this.removePrefix("generating:")
            return !activeTaskIds.contains(taskId) // Needs generation if it's stuck (not in active tasks)
        }
        if (this.contains("dummyimage")) return true
        return !this.contains("http")
    }
}
