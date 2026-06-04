package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.Serializable

@Serializable
data class SubclassDto(
    val index: String = "",
    val name: String = "",
    val `class`: ApiReferenceDto = ApiReferenceDto(),
    val subclass_flavor: String = "",
    val subclass_levels: String = "", // URL
    val desc: List<String> = emptyList(),
    val spells: List<SubclassSpellDto>? = null,
    val url: String = "",
)

@Serializable
data class SubclassSpellDto(
    val prerequisites: List<SpellPrerequisiteDto> = emptyList(),
    val spell: ApiReferenceDto = ApiReferenceDto(),
)

@Serializable
data class SpellPrerequisiteDto(
    val index: String = "",
    val name: String = "",
    val type: String = "", // "level"
    val url: String = "",
)
