package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DamageDto(
    @SerialName("damage_dice") val damageDice: String = "",
    @SerialName("damage_type") val damageType: ApiReferenceDto = ApiReferenceDto(),
)
