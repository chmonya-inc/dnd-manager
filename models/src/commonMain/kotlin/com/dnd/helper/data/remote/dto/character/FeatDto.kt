package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.PrerequisiteDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("prerequisites") val prerequisites: List<PrerequisiteDto> = emptyList(),
    @SerialName("url") val url: String = "",
)
