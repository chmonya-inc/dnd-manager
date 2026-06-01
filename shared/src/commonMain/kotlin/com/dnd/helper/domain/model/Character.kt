package com.dnd.helper.domain.model

import com.dnd.helper.domain.utils.ImageUrlHelper
import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: String,
    val name: String,
    val playerName: String,
    val race: String,
    val characterClass: String,
    val subclass: String = "",
    val background: String = "",
    val level: Int,
    val experiencePoints: Int = 0,
    val description: String,
    val imageUrl: String? = null,
    val appearance: CharacterAppearance = CharacterAppearance(),
    val stats: CharacterStats = CharacterStats(),
    val maxHp: Int,
    val currentHp: Int,
    val combat: CharacterCombat = CharacterCombat(),
    val proficiencies: CharacterProficiencies = CharacterProficiencies(),
    val weapons: List<Weapon> = emptyList(),
    val features: CharacterFeatures = CharacterFeatures(),
    val skills: List<Skill> = emptyList(),
    val items: List<Item> = emptyList(),
    val notes: List<Note> = emptyList(),
) {
    val displayImageUrl: String? get() = ImageUrlHelper.process(imageUrl)
}
