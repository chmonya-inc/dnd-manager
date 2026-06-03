package com.dnd.helper.domain.model

import com.dnd.helper.domain.utils.ImageUrlHelper
import kotlinx.serialization.Serializable

@Serializable
data class Monster(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val stats: CharacterStats = CharacterStats(),
    val maxHp: Int = 10,
    val currentHp: Int = 10,
    val armorClass: Int = 10,
    val speed: Int = 30,
    val challengeRating: String = "1",
    val type: String = "Humanoid",
    val alignment: String = "Neutral",
    val size: String = "Medium",
    val proficiencies: List<String> = emptyList(),
    val conditionImmunities: List<String> = emptyList(),
    val damageImmunities: List<String> = emptyList(),
    val damageResistances: List<String> = emptyList(),
    val damageVulnerabilities: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val specialAbilities: List<MonsterAction> = emptyList(),
    val actions: List<MonsterAction> = emptyList(),
    val legendaryActions: List<MonsterAction> = emptyList(),
    val reactions: List<MonsterAction> = emptyList()
) {
    val displayImageUrl: String? get() = ImageUrlHelper.process(imageUrl)
}

@Serializable
data class MonsterAction(
    val name: String,
    val description: String
)
