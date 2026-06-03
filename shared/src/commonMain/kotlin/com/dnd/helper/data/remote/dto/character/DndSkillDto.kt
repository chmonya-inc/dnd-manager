package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.Serializable

/** Official D&D 5e skill (Acrobatics, Athletics, etc.) from the API. */
@Serializable
data class DndSkillDto(
    val index: String = "",  // "acrobatics", "athletics", etc.
    val name: String = "",
    val desc: List<String> = emptyList(),
    val ability_score: ApiReferenceDto = ApiReferenceDto(),
    val url: String = "",
)
