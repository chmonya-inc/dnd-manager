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
