package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterCombat(
    val armorClass: Int = 10,
    val initiative: Int = 0,
    val speed: Int = 30,
    val proficiencyBonus: Int = 2,
    val tempHp: Int = 0,
    val hitDice: String = "1d8",
    val hitDiceCurrent: Int = 1,
    val inspiration: Boolean = false,
    val exhaustion: Int = 0,
    val conditions: List<String> = emptyList(),
    val deathSaveSuccesses: Int = 0,
    val deathSaveFailures: Int = 0,
)
