package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterAppearance(
    val age: Int = 0,
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val eyes: String = "",
    val hair: String = "",
    val skin: String = "",
)
