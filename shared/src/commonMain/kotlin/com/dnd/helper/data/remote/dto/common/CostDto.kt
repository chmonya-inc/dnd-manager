package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class CostDto(
    val quantity: Double = 0.0,
    val unit: String = "gp", // "cp" | "sp" | "ep" | "gp" | "pp"
)
