package com.dnd.helper.data.remote.dto.character

import kotlinx.serialization.Serializable

@Serializable
data class AlignmentDto(
    val index: String = "",
    val name: String = "",         // "Chaotic Neutral"
    val abbreviation: String = "", // "CN"
    val desc: String = "",
    val url: String = "",
)
