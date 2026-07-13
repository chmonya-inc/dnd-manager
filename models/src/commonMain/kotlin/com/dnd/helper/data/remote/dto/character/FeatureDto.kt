package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeatureDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("level") val level: Int = 1,
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("class") val `class`: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("subclass") val subclass: ApiReferenceDto? = null,
    @SerialName("parent") val parent: ApiReferenceDto? = null,
    @SerialName("prerequisites") val prerequisites: List<FeaturePrerequisiteDto> = emptyList(),
    @SerialName("feature_specific") val featureSpecific: FeatureSpecificDto? = null,
    @SerialName("url") val url: String = "",
)

@Serializable
data class FeaturePrerequisiteDto(
    @SerialName("type") val type: String = "", // "level" | "feature" | "spell"
    @SerialName("level") val level: Int? = null,
    @SerialName("feature") val feature: String? = null,
    @SerialName("spell") val spell: String? = null,
)

@Serializable
data class FeatureSpecificDto(
    @SerialName("subfeature_options") val subfeatureOptions: ChoiceDto? = null,
    @SerialName("expertise_options") val expertiseOptions: ChoiceDto? = null,
    @SerialName("terrain_type_options") val terrainTypeOptions: ChoiceDto? = null,
    @SerialName("enemy_type_options") val enemyTypeOptions: ChoiceDto? = null,
    @SerialName("invocations") val invocations: List<ApiReferenceDto>? = null,
)
