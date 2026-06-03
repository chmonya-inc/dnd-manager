package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiReferenceDto(
    val index: String = "",
    val name: String = "",
    val url: String = "",
    val updated_at: String? = null,
)

@Serializable
data class ApiReferenceListDto(
    val count: Int = 0,
    val results: List<ApiReferenceDto> = emptyList(),
)
