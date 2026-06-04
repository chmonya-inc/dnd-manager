package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.PrerequisiteDto
import kotlinx.serialization.Serializable

@Serializable
data class FeatDto(
    val index: String = "",
    val name: String = "",
    val desc: List<String> = emptyList(),
    val prerequisites: List<PrerequisiteDto> = emptyList(),
    val url: String = "",
)
