package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class EquipmentSlot {
    HEAD, BODY, HANDS, FEET, MAIN_HAND, OFF_HAND, RING, AMULET
}

@Serializable
enum class ItemRarity {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
}

@Serializable
data class Item(
    val id: String,
    val name: String,
    val slot: EquipmentSlot?,
    val rarity: ItemRarity,
    val stats: Map<String, Int> = emptyMap(),
    val description: String = "",
    val equipped: Boolean = false,
    val imageUrl: String? = null,
)
