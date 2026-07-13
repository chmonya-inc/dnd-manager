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

fun abilityModifier(score: Int): Int = if (score >= 10) (score - 10) / 2 else (score - 11) / 2

fun CharacterStats.modifier(ability: String): Int = when (ability.lowercase()) {
    "strength", "str" -> abilityModifier(strength)
    "dexterity", "dex" -> abilityModifier(dexterity)
    "constitution", "con" -> abilityModifier(constitution)
    "intelligence", "int" -> abilityModifier(intelligence)
    "wisdom", "wis" -> abilityModifier(wisdom)
    "charisma", "cha" -> abilityModifier(charisma)
    else -> 0
}

fun CharacterStats.score(ability: String): Int = when (ability.lowercase()) {
    "strength", "str" -> strength
    "dexterity", "dex" -> dexterity
    "constitution", "con" -> constitution
    "intelligence", "int" -> intelligence
    "wisdom", "wis" -> wisdom
    "charisma", "cha" -> charisma
    else -> 10
}
