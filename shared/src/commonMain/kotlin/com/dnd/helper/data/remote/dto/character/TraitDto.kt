package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.AreaOfEffectDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import com.dnd.helper.data.remote.dto.common.DcDto
import kotlinx.serialization.Serializable

@Serializable
data class TraitDto(
    val index: String = "",
    val name: String = "",
    val desc: List<String> = emptyList(),
    val races: List<ApiReferenceDto> = emptyList(),
    val subraces: List<ApiReferenceDto> = emptyList(),
    val proficiencies: List<ApiReferenceDto> = emptyList(),
    val proficiency_choices: ChoiceDto? = null,
    val language_options: ChoiceDto? = null,
    val trait_specific: TraitSpecificDto? = null,
    val url: String = "",
)

@Serializable
data class TraitSpecificDto(
    val damage_type: ApiReferenceDto? = null,
    val breath_weapon: BreathWeaponDto? = null,
    val subtrait_options: ChoiceDto? = null,
    val spell_options: ChoiceDto? = null,
)

@Serializable
data class BreathWeaponDto(
    val name: String = "",
    val desc: String = "",
    val area_of_effect: AreaOfEffectDto = AreaOfEffectDto(),
    val damage: BreathWeaponDamageDto = BreathWeaponDamageDto(),
    val dc: DcDto = DcDto(),
    val usage: TraitUsageDto = TraitUsageDto(),
)

@Serializable
data class BreathWeaponDamageDto(
    val damage_at_character_level: Map<String, String> = emptyMap(),
    val damage_type: ApiReferenceDto = ApiReferenceDto(),
)

@Serializable
data class TraitUsageDto(
    val times: Int = 1,
    val type: String = "per day",
)
