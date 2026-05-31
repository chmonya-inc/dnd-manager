package com.dnd.helper.presentation.charactercreate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterCreateViewModel(
    private val repository: CharacterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterCreateState())
    val state: StateFlow<CharacterCreateState> = _state.asStateFlow()

    fun onEvent(event: CharacterCreateEvent) {
        when (event) {
            is CharacterCreateEvent.NameChanged -> _state.value = _state.value.copy(name = event.value)
            is CharacterCreateEvent.PlayerNameChanged -> _state.value = _state.value.copy(playerName = event.value)
            is CharacterCreateEvent.RaceChanged -> _state.value = _state.value.copy(race = event.value)
            is CharacterCreateEvent.ClassChanged -> _state.value = _state.value.copy(characterClass = event.value)
            is CharacterCreateEvent.LevelChanged -> _state.value = _state.value.copy(level = event.value)
            is CharacterCreateEvent.DescriptionChanged -> _state.value = _state.value.copy(description = event.value)
            is CharacterCreateEvent.ImageUrlChanged -> _state.value = _state.value.copy(imageUrl = event.value)
            is CharacterCreateEvent.MaxHpChanged -> _state.value = _state.value.copy(maxHp = event.value)
            is CharacterCreateEvent.CurrentHpChanged -> _state.value = _state.value.copy(currentHp = event.value)
            is CharacterCreateEvent.StrengthChanged -> _state.value = _state.value.copy(strength = event.value)
            is CharacterCreateEvent.DexterityChanged -> _state.value = _state.value.copy(dexterity = event.value)
            is CharacterCreateEvent.ConstitutionChanged -> _state.value = _state.value.copy(constitution = event.value)
            is CharacterCreateEvent.IntelligenceChanged -> _state.value = _state.value.copy(intelligence = event.value)
            is CharacterCreateEvent.WisdomChanged -> _state.value = _state.value.copy(wisdom = event.value)
            is CharacterCreateEvent.CharismaChanged -> _state.value = _state.value.copy(charisma = event.value)
            CharacterCreateEvent.AddItem -> addItem()
            is CharacterCreateEvent.RemoveItem -> removeItem(event.index)
            is CharacterCreateEvent.ItemNameChanged -> updateItem(event.index) { it.copy(name = event.value) }
            is CharacterCreateEvent.ItemSlotChanged -> updateItem(event.index) { it.copy(slot = event.value) }
            is CharacterCreateEvent.ItemRarityChanged -> updateItem(event.index) { it.copy(rarity = event.value) }
            is CharacterCreateEvent.ItemDescriptionChanged -> updateItem(event.index) { it.copy(description = event.value) }
            is CharacterCreateEvent.ItemEquippedChanged -> updateItem(event.index) { it.copy(equipped = event.value) }
            CharacterCreateEvent.SaveCharacter -> saveCharacter()
        }
    }

    private fun addItem() {
        val newItem = Item(
            id = "item-${System.currentTimeMillis()}",
            name = "New Item",
            slot = EquipmentSlot.MAIN_HAND,
            rarity = ItemRarity.COMMON,
            description = "",
            equipped = false,
        )
        _state.value = _state.value.copy(items = _state.value.items + newItem)
    }

    private fun removeItem(index: Int) {
        val current = _state.value.items
        if (index in current.indices) {
            _state.value = _state.value.copy(items = current.filterIndexed { i, _ -> i != index })
        }
    }

    private fun updateItem(index: Int, transform: (Item) -> Item) {
        val current = _state.value.items
        if (index in current.indices) {
            _state.value = _state.value.copy(
                items = current.mapIndexed { i, item ->
                    if (i == index) transform(item) else item
                }
            )
        }
    }

    private fun saveCharacter() {
        val s = _state.value

        val level = s.level.toIntOrNull() ?: 1
        val maxHp = s.maxHp.toIntOrNull() ?: 10
        val currentHp = s.currentHp.toIntOrNull() ?: maxHp
        val strength = s.strength.toIntOrNull() ?: 10
        val dexterity = s.dexterity.toIntOrNull() ?: 10
        val constitution = s.constitution.toIntOrNull() ?: 10
        val intelligence = s.intelligence.toIntOrNull() ?: 10
        val wisdom = s.wisdom.toIntOrNull() ?: 10
        val charisma = s.charisma.toIntOrNull() ?: 10

        if (s.name.isBlank()) {
            _state.value = s.copy(error = "Name is required")
            return
        }

        val character = Character(
            id = "char-${System.currentTimeMillis()}",
            name = s.name.trim(),
            playerName = s.playerName.trim(),
            race = s.race.trim(),
            characterClass = s.characterClass.trim(),
            level = level,
            description = s.description.trim(),
            imageUrl = s.imageUrl.trim().ifBlank { null },
            maxHp = maxHp,
            currentHp = currentHp.coerceIn(0, maxHp),
            stats = CharacterStats(
                strength = strength,
                dexterity = dexterity,
                constitution = constitution,
                intelligence = intelligence,
                wisdom = wisdom,
                charisma = charisma,
            ),
            items = s.items,
        )

        _state.value = s.copy(isSaving = true, error = null)

        viewModelScope.launch {
            when (val result = repository.saveCharacter(character)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(isSaving = false, isSaved = true)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = result.error.toUserMessage(),
                    )
                }
            }
        }
    }
}
