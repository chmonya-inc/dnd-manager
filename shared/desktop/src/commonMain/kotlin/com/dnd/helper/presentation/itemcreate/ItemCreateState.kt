package com.dnd.helper.presentation.itemcreate

import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.ItemRarity

data class ItemCreateState(
    val characterId: String = "",
    val itemId: String? = null,
    val name: String = "",
    val description: String = "",
    val slot: EquipmentSlot? = EquipmentSlot.MAIN_HAND,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val cost: String = "",
    val weight: String = "",
    val type: String = "",
    val properties: List<String> = emptyList(),
    val isEquipped: Boolean = false,
    val stats: Map<String, Int> = emptyMap(),
    val availableProperties: List<String> = emptyList(),
    val availableEquipmentCategories: List<String> = emptyList(),
    val characters: List<com.dnd.helper.domain.model.Character> = emptyList(),
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val isSaveSuccessful: Boolean = false,
    val isGeneratingAi: Boolean = false,
    val imageUrl: String = "",
    val aiPrompt: String = "",
    val aiWidth: String = "256",
    val aiHeight: String = "256"
)
