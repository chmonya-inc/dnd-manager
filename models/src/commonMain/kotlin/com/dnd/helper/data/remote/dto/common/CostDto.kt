package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CostDto(
    @SerialName("quantity") val quantity: Double = 0.0,
    @SerialName("unit") val unit: String = "gp", // "cp" | "sp" | "ep" | "gp" | "pp"
)
