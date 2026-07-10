package com.dnd.helper.data.remote.dto.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConditionDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class DamageTypeDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)
