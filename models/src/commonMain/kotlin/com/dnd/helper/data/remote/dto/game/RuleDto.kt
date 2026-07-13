package com.dnd.helper.data.remote.dto.game

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RuleDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("subsections") val subsections: List<ApiReferenceDto> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class RuleSectionDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("url") val url: String = "",
)
