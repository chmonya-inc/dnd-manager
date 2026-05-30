package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Character(
    val id: String,
    val name: String,
    val playerName: String,
    val race: String,
    val characterClass: String,
    val level: Int,
    val description: String,
    val imageUrl: String? = null,
    val stats: CharacterStats = CharacterStats(),
    val maxHp: Int,
    val currentHp: Int,
)
