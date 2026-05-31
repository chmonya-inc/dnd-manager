package com.dnd.helper.presentation.charactercreate

import com.dnd.helper.domain.model.Item

data class CharacterCreateState(
    val name: String = "",
    val playerName: String = "",
    val race: String = "",
    val characterClass: String = "",
    val level: String = "1",
    val description: String = "",
    val imageUrl: String = "",
    val maxHp: String = "10",
    val currentHp: String = "10",
    val strength: String = "10",
    val dexterity: String = "10",
    val constitution: String = "10",
    val intelligence: String = "10",
    val wisdom: String = "10",
    val charisma: String = "10",
    val items: List<Item> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)
