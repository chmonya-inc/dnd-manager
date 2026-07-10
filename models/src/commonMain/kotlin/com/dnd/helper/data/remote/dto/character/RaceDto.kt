package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RaceDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("speed") val speed: Int = 30,
    @SerialName("ability_bonuses") val abilityBonuses: List<AbilityBonusDto> = emptyList(),
    @SerialName("alignment") val alignment: String = "",
    @SerialName("age") val age: String = "",
    @SerialName("size") val size: String = "",
    @SerialName("size_description") val sizeDescription: String = "",
    @SerialName("languages") val languages: List<ApiReferenceDto> = emptyList(),
    @SerialName("language_desc") val languageDesc: String = "",
    @SerialName("language_options") val languageOptions: AbilityBonusChoiceDto? = null,
    @SerialName("traits") val traits: List<ApiReferenceDto> = emptyList(),
    @SerialName("subraces") val subraces: List<ApiReferenceDto> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class SubraceDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("race") val race: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("ability_bonuses") val abilityBonuses: List<AbilityBonusDto> = emptyList(),
    @SerialName("racial_traits") val racialTraits: List<ApiReferenceDto> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class AbilityBonusDto(
    @SerialName("ability_score") val abilityScore: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("bonus") val bonus: Int = 0,
)

/** Placeholder for language choice in some races. */
@Serializable
data class AbilityBonusChoiceDto(
    @SerialName("choose") val choose: Int = 1,
    @SerialName("type") val type: String = "",
)
