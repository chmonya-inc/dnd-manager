package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Official D&D 5e skill (Acrobatics, Athletics, etc.) from the API. */
@Serializable
data class DndSkillDto(
    @SerialName("index") val index: String = "", // "acrobatics", "athletics", etc.
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("ability_score") val abilityScore: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("url") val url: String = "",
)
