package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AreaOfEffectDto(
    @SerialName("size") val size: Int = 0,
    @SerialName("type") val type: String = "", // "sphere" | "cone" | "cylinder" | "line" | "cube"
)
