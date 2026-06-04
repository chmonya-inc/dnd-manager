package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterStats(
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
)

fun abilityModifier(score: Int): Int = (score - 10) / 2

fun CharacterStats.modifier(ability: String): Int = when (ability.lowercase()) {
    "strength" -> abilityModifier(strength)
    "dexterity" -> abilityModifier(dexterity)
    "constitution" -> abilityModifier(constitution)
    "intelligence" -> abilityModifier(intelligence)
    "wisdom" -> abilityModifier(wisdom)
    "charisma" -> abilityModifier(charisma)
    else -> 0
}

fun CharacterStats.score(ability: String): Int = when (ability.lowercase()) {
    "strength" -> strength
    "dexterity" -> dexterity
    "constitution" -> constitution
    "intelligence" -> intelligence
    "wisdom" -> wisdom
    "charisma" -> charisma
    else -> 10
}
