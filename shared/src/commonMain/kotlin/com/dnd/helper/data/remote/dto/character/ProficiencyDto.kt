package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.Serializable

@Serializable
data class ProficiencyDto(
    val index: String = "",
    val name: String = "",
    val type: String = "", // "Armor" | "Weapons" | "Artisan's Tools" | "Skills" | "Saving Throws"
    val classes: List<ApiReferenceDto> = emptyList(),
    val races: List<ApiReferenceDto> = emptyList(),
    val reference: ApiReferenceDto? = null,
    val url: String = "",
)
