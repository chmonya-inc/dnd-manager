package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PrerequisiteDto(
    @SerialName("ability_score") val abilityScore: ApiReferenceDto? = null,
    @SerialName("minimum_score") val minimumScore: Double? = null,
)
