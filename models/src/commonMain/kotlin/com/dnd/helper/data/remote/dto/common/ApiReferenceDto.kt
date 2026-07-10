package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiReferenceDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("url") val url: String = "",
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ApiReferenceListDto(
    @SerialName("count") val count: Int = 0,
    @SerialName("results") val results: List<ApiReferenceDto> = emptyList(),
)
