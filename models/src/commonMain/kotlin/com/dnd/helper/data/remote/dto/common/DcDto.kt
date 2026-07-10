package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DcDto(
    @SerialName("dc_type") val dcType: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("dc_value") val dcValue: Double? = null,
    @SerialName("success_type") val successType: String = "none", // "none" | "half" | "other"
)
