package com.dnd.helper.data.remote.dto.character

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AlignmentDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "", // "Chaotic Neutral"
    @SerialName("abbreviation") val abbreviation: String = "", // "CN"
    @SerialName("desc") val desc: String = "",
    @SerialName("url") val url: String = "",
)
