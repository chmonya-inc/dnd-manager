package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubclassDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("class") val `class`: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("subclass_flavor") val subclassFlavor: String = "",
    @SerialName("subclass_levels") val subclassLevels: String = "", // URL
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("spells") val spells: List<SubclassSpellDto>? = null,
    @SerialName("url") val url: String = "",
)

@Serializable
data class SubclassSpellDto(
    @SerialName("prerequisites") val prerequisites: List<SpellPrerequisiteDto> = emptyList(),
    @SerialName("spell") val spell: ApiReferenceDto = ApiReferenceDto(),
)

@Serializable
data class SpellPrerequisiteDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("type") val type: String = "", // "level"
    @SerialName("url") val url: String = "",
)
