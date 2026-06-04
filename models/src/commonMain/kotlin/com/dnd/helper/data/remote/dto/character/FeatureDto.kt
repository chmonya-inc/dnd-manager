package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import kotlinx.serialization.Serializable

@Serializable
data class FeatureDto(
    val index: String = "",
    val name: String = "",
    val level: Int = 1,
    val desc: List<String> = emptyList(),
    val `class`: ApiReferenceDto = ApiReferenceDto(),
    val subclass: ApiReferenceDto? = null,
    val parent: ApiReferenceDto? = null,
    val prerequisites: List<FeaturePrerequisiteDto> = emptyList(),
    val feature_specific: FeatureSpecificDto? = null,
    val url: String = "",
)

@Serializable
data class FeaturePrerequisiteDto(
    val type: String = "",       // "level" | "feature" | "spell"
    val level: Int? = null,
    val feature: String? = null,
    val spell: String? = null,
)

@Serializable
data class FeatureSpecificDto(
    val subfeature_options: ChoiceDto? = null,
    val expertise_options: ChoiceDto? = null,
    val terrain_type_options: ChoiceDto? = null,
    val enemy_type_options: ChoiceDto? = null,
    val invocations: List<ApiReferenceDto>? = null,
)
