package com.dnd.helper.presentation.itemcreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.DndApiDataSource
import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.data.remote.PromptGenerator
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.repository.EditingRepository
import com.dnd.helper.domain.repository.GenerationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ItemCreateViewModel(
    private val repository: CharacterRepository,
    private val editingRepository: EditingRepository,
    private val api: DndApiDataSource
) : ViewModel() {

    private val tempId = "temp-item-${kotlin.random.Random.nextInt(1000000, 9999999)}"
    private val _state = MutableStateFlow(ItemCreateState())
    val state: StateFlow<ItemCreateState> = _state.asStateFlow()

    private var isInitialized = false

    fun initData(existingItem: Item?, ownerId: String?) {
        if (isInitialized) return
        isInitialized = true

        _state.update { currentState ->
            if (existingItem != null) {
                currentState.copy(
                    itemId = existingItem.id,
                    name = existingItem.name,
                    slot = existingItem.slot,
                    rarity = existingItem.rarity,
                    stats = existingItem.stats,
                    description = existingItem.description,
                    isEquipped = existingItem.equipped,
                    cost = existingItem.cost,
                    weight = existingItem.weight.toString(),
                    type = existingItem.type,
                    properties = existingItem.properties,
                    imageUrl = existingItem.imageUrl ?: "",
                    characterId = ownerId ?: ""
                )
            } else {
                currentState.copy(characterId = ownerId ?: "")
            }
        }
        updateDefaultPrompt()
    }

    init {
        viewModelScope.launch {
            editingRepository.activeTasks.collect { tasks ->
                tasks.forEach { task ->
                    if (task.status == GenerationStatus.COMPLETED && task.resultUrl != null) {
                        var changed = false
                        _state.update { currentState ->
                            if (task.entityType == "item" && currentState.imageUrl == "generating:${task.id}") {
                                changed = true
                                currentState.copy(imageUrl = task.resultUrl!!)
                            } else {
                                currentState
                            }
                        }

                        // If the image was successfully generated while this screen is open,
                        // and we already have a character owner, auto-save the update
                        // so the parent screen picks it up immediately.
                        if (changed && _state.value.characterId.isNotBlank()) {
                            saveItem()
                        }
                    }
                }
            }
        }
        fetchApiData()
    }

    private fun fetchApiData() {
        viewModelScope.launch {
            val propsResult = api.getWeaponProperties()
            val categoriesResult = api.getEquipmentCategories()
            val charsResult = repository.getCharacters()

            val props = if (propsResult is Result.Success) propsResult.data.results.map { it.name } else emptyList()
            val categories = if (categoriesResult is Result.Success) categoriesResult.data.results.map { it.name } else emptyList()
            val chars = if (charsResult is Result.Success) charsResult.data else emptyList()

            _state.update {
                it.copy(
                    availableProperties = props,
                    availableEquipmentCategories = categories,
                    characters = chars,
                    characterId = if (it.characterId.isEmpty() && chars.isNotEmpty()) chars.first().id else it.characterId
                )
            }
        }
    }

    fun onEvent(event: ItemCreateEvent) {
        when (event) {
            is ItemCreateEvent.NameChanged -> {
                _state.update { it.copy(name = event.name) }
                updateDefaultPrompt()
            }
            is ItemCreateEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description) }
                updateDefaultPrompt()
            }
            is ItemCreateEvent.SlotChanged -> _state.update { it.copy(slot = event.slot) }
            is ItemCreateEvent.RarityChanged -> _state.update { it.copy(rarity = event.rarity) }
            is ItemCreateEvent.CostChanged -> _state.update { it.copy(cost = event.cost) }
            is ItemCreateEvent.WeightChanged -> _state.update { it.copy(weight = event.weight) }
            is ItemCreateEvent.TypeChanged -> _state.update { it.copy(type = event.type) }
            is ItemCreateEvent.EquippedChanged -> _state.update { it.copy(isEquipped = event.equipped) }
            is ItemCreateEvent.AiPromptChanged -> _state.update { it.copy(aiPrompt = event.prompt) }
            is ItemCreateEvent.AiWidthChanged -> _state.update { it.copy(aiWidth = event.width) }
            is ItemCreateEvent.AiHeightChanged -> _state.update { it.copy(aiHeight = event.height) }
            is ItemCreateEvent.ImageUrlChanged -> _state.update { it.copy(imageUrl = event.url) }
            ItemCreateEvent.GenerateImageClicked -> generateImage()
            is ItemCreateEvent.PropertyToggled -> {
                _state.update { currentState ->
                    val props = currentState.properties.toMutableList()
                    if (props.contains(event.property)) {
                        props.remove(event.property)
                    } else {
                        props.add(event.property)
                    }
                    currentState.copy(properties = props)
                }
            }
            is ItemCreateEvent.OwnerChanged -> _state.update { it.copy(characterId = event.characterId) }
            ItemCreateEvent.SaveClicked -> saveItem()
            ItemCreateEvent.BackClicked -> { /* Handled by UI */ }
        }
    }

    private fun updateDefaultPrompt() {
        val s = _state.value
        val promptText = "${s.name}. ${s.description}".trim()
        if (promptText.isNotBlank()) {
            val fullPrompt = PromptGenerator.getFullPrompt(promptText, GenerationType.ITEM)
            _state.update { it.copy(aiPrompt = fullPrompt) }
        }
    }

    private fun saveItem() {
        val currentState = _state.value
        _state.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            val weightDouble = currentState.weight.toDoubleOrNull() ?: 0.0
            val itemToSave = Item(
                id = currentState.itemId ?: tempId,
                name = currentState.name.ifBlank { "Unnamed Item" },
                slot = currentState.slot,
                rarity = currentState.rarity,
                stats = currentState.stats,
                description = currentState.description,
                equipped = currentState.isEquipped,
                cost = currentState.cost,
                weight = weightDouble,
                type = currentState.type,
                properties = currentState.properties,
                imageUrl = currentState.imageUrl.takeIf { it.isNotBlank() }
            )

            // Also assign to character if needed
            val char = currentState.characters.find { it.id == currentState.characterId }
            val result = if (char != null) {
                val itemExists = char.items.any { it.id == itemToSave.id }
                val newItems = if (itemExists) {
                    char.items.map { if (it.id == itemToSave.id) itemToSave else it }
                } else {
                    char.items + itemToSave
                }
                repository.saveCharacter(char.copy(items = newItems))
            } else {
                // If no character, we can't save the item since it's connected to character
                Result.Error(AppError.Unknown("Item must have an owner to be saved"))
            }

            if (result is Result.Success) {
                _state.update { it.copy(isSaving = false, isSaveSuccessful = true) }
            } else {
                val error = when (result) {
                    is Result.Error -> result.error.toUserMessage()
                    else -> "Failed to save item"
                }
                _state.update { it.copy(isSaving = false, saveError = error) }
            }
        }
    }

    private fun generateImage() {
        val s = _state.value
        if (s.aiPrompt.isBlank()) return

        val actualId = s.itemId ?: tempId
        val charId = s.characterId.ifBlank { "item-only" }
        val entityId = "$charId:$actualId"

        viewModelScope.launch {
            val taskId = editingRepository.startGeneration(
                entityId = entityId,
                entityType = "item",
                prompt = s.aiPrompt,
                genType = GenerationType.ITEM,
                width = s.aiWidth.toIntOrNull() ?: 1024,
                height = s.aiHeight.toIntOrNull() ?: 1024
            )
            _state.update { it.copy(imageUrl = taskId) }
        }
    }
}
