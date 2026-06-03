package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.Serializable

@Serializable
data class AbilityScoreDto(
    val index: String = "",
    val name: String = "",         // "STR", "DEX", etc.
    val full_name: String = "",    // "Strength", "Dexterity", etc.
    val desc: List<String> = emptyList(),
    val skills: List<ApiReferenceDto> = emptyList(),
    val url: String = "",
)
