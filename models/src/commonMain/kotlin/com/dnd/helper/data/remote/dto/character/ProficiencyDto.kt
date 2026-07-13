package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProficiencyDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: String = "", // "Armor" | "Weapons" | "Artisan's Tools" | "Skills" | "Saving Throws"
    @SerialName("classes") val classes: List<ApiReferenceDto> = emptyList(),
    @SerialName("races") val races: List<ApiReferenceDto> = emptyList(),
    @SerialName("reference") val reference: ApiReferenceDto? = null,
    @SerialName("url") val url: String = "",
)
