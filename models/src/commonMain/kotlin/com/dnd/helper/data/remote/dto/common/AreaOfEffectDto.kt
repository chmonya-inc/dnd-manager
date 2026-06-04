package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class AreaOfEffectDto(
    val size: Int = 0,
    val type: String = "", // "sphere" | "cone" | "cylinder" | "line" | "cube"
)
