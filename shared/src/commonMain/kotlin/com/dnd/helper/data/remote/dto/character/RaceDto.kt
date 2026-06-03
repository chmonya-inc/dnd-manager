package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.Serializable

@Serializable
data class RaceDto(
    val index: String = "",
    val name: String = "",
    val speed: Int = 30,
    val ability_bonuses: List<AbilityBonusDto> = emptyList(),
    val alignment: String = "",
    val age: String = "",
    val size: String = "",
    val size_description: String = "",
    val languages: List<ApiReferenceDto> = emptyList(),
    val language_desc: String = "",
    val language_options: AbilityBonusChoiceDto? = null,
    val traits: List<ApiReferenceDto> = emptyList(),
    val subraces: List<ApiReferenceDto> = emptyList(),
    val url: String = "",
)

@Serializable
data class SubraceDto(
    val index: String = "",
    val name: String = "",
    val desc: String = "",
    val race: ApiReferenceDto = ApiReferenceDto(),
    val ability_bonuses: List<AbilityBonusDto> = emptyList(),
    val racial_traits: List<ApiReferenceDto> = emptyList(),
    val url: String = "",
)

@Serializable
data class AbilityBonusDto(
    val ability_score: ApiReferenceDto = ApiReferenceDto(),
    val bonus: Int = 0,
)

/** Placeholder for language choice in some races. */
@Serializable
data class AbilityBonusChoiceDto(
    val choose: Int = 1,
    val type: String = "",
)
