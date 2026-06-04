package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class PrerequisiteDto(
    val ability_score: ApiReferenceDto? = null,
    val minimum_score: Double? = null,
)
