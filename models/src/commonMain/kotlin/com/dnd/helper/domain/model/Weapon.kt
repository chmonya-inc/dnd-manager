package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Weapon(
    val id: String,
    val name: String,
    val attackBonus: String = "",
    val damage: String = "",
    val damageType: String = "",
    val notes: String = "",
)
