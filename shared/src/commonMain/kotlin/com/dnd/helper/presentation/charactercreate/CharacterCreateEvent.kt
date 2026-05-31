package com.dnd.helper.presentation.charactercreate

import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.ItemRarity

sealed interface CharacterCreateEvent {
    data class NameChanged(val value: String) : CharacterCreateEvent
    data class PlayerNameChanged(val value: String) : CharacterCreateEvent
    data class RaceChanged(val value: String) : CharacterCreateEvent
    data class ClassChanged(val value: String) : CharacterCreateEvent
    data class LevelChanged(val value: String) : CharacterCreateEvent
    data class DescriptionChanged(val value: String) : CharacterCreateEvent
    data class ImageUrlChanged(val value: String) : CharacterCreateEvent
    data class MaxHpChanged(val value: String) : CharacterCreateEvent
    data class CurrentHpChanged(val value: String) : CharacterCreateEvent
    data class StrengthChanged(val value: String) : CharacterCreateEvent
    data class DexterityChanged(val value: String) : CharacterCreateEvent
    data class ConstitutionChanged(val value: String) : CharacterCreateEvent
    data class IntelligenceChanged(val value: String) : CharacterCreateEvent
    data class WisdomChanged(val value: String) : CharacterCreateEvent
    data class CharismaChanged(val value: String) : CharacterCreateEvent
    data object AddItem : CharacterCreateEvent
    data class RemoveItem(val index: Int) : CharacterCreateEvent
    data class ItemNameChanged(val index: Int, val value: String) : CharacterCreateEvent
    data class ItemSlotChanged(val index: Int, val value: EquipmentSlot?) : CharacterCreateEvent
    data class ItemRarityChanged(val index: Int, val value: ItemRarity) : CharacterCreateEvent
    data class ItemDescriptionChanged(val index: Int, val value: String) : CharacterCreateEvent
    data class ItemEquippedChanged(val index: Int, val value: Boolean) : CharacterCreateEvent
    data object SaveCharacter : CharacterCreateEvent
}
