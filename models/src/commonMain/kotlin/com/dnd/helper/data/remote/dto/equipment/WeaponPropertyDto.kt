package com.dnd.helper.data.remote.dto.equipment

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeaponPropertyDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)
