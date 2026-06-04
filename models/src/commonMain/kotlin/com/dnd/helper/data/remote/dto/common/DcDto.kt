package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class DcDto(
    val dc_type: ApiReferenceDto = ApiReferenceDto(),
    val dc_value: Double? = null,
    val success_type: String = "none", // "none" | "half" | "other"
)
