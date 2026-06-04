package com.dnd.helper.data.remote.dto.game

import kotlinx.serialization.Serializable

@Serializable
data class ConditionDto(
    val index: String = "",
    val name: String = "",
    val desc: List<String> = emptyList(),
    val url: String = "",
)

@Serializable
data class DamageTypeDto(
    val index: String = "",
    val name: String = "",
    val desc: List<String> = emptyList(),
    val url: String = "",
)
