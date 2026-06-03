package com.dnd.helper.data.remote.dto.character

import kotlinx.serialization.Serializable

@Serializable
data class LanguageDto(
    val index: String = "",
    val name: String = "",
    val type: String = "Standard", // "Standard" | "Exotic"
    val desc: String? = null,
    val script: String? = null,
    val typical_speakers: List<String> = emptyList(),
    val url: String = "",
)
