package com.dnd.helper.data.remote.dto.game

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.Serializable

@Serializable
data class RuleDto(
    val index: String = "",
    val name: String = "",
    val desc: String = "",
    val subsections: List<ApiReferenceDto> = emptyList(),
    val url: String = "",
)

@Serializable
data class RuleSectionDto(
    val index: String = "",
    val name: String = "",
    val desc: String = "",
    val url: String = "",
)
