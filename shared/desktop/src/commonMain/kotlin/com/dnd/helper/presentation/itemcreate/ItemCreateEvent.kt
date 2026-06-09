package com.dnd.helper.presentation.itemcreate

import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.ItemRarity

sealed interface ItemCreateEvent {
    data class NameChanged(val name: String) : ItemCreateEvent
    data class DescriptionChanged(val description: String) : ItemCreateEvent
    data class SlotChanged(val slot: EquipmentSlot?) : ItemCreateEvent
    data class RarityChanged(val rarity: ItemRarity) : ItemCreateEvent
    data class CostChanged(val cost: String) : ItemCreateEvent
    data class WeightChanged(val weight: String) : ItemCreateEvent
    data class TypeChanged(val type: String) : ItemCreateEvent
    data class PropertyToggled(val property: String) : ItemCreateEvent
    data class EquippedChanged(val equipped: Boolean) : ItemCreateEvent
    data class AiPromptChanged(val prompt: String) : ItemCreateEvent
    data class AiWidthChanged(val width: String) : ItemCreateEvent
    data class AiHeightChanged(val height: String) : ItemCreateEvent
    data class ImageUrlChanged(val url: String) : ItemCreateEvent
    object GenerateImageClicked : ItemCreateEvent
    data class OwnerChanged(val characterId: String) : ItemCreateEvent
    object SaveClicked : ItemCreateEvent
    object BackClicked : ItemCreateEvent
}
