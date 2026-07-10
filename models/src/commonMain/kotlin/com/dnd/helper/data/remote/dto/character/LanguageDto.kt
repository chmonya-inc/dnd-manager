package com.dnd.helper.data.remote.dto.character

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LanguageDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: String = "Standard", // "Standard" | "Exotic"
    @SerialName("desc") val desc: String? = null,
    @SerialName("script") val script: String? = null,
    @SerialName("typical_speakers") val typicalSpeakers: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)
