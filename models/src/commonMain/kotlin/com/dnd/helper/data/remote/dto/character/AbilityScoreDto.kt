package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AbilityScoreDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "", // "STR", "DEX", etc.
    @SerialName("full_name") val fullName: String = "", // "Strength", "Dexterity", etc.
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("skills") val skills: List<ApiReferenceDto> = emptyList(),
    @SerialName("url") val url: String = "",
)
