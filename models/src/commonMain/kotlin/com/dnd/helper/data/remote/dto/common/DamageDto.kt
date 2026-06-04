package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class DamageDto(
    val damage_dice: String = "",
    val damage_type: ApiReferenceDto = ApiReferenceDto(),
)
