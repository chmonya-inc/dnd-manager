package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.AreaOfEffectDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import com.dnd.helper.data.remote.dto.common.DcDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TraitDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("races") val races: List<ApiReferenceDto> = emptyList(),
    @SerialName("subraces") val subraces: List<ApiReferenceDto> = emptyList(),
    @SerialName("proficiencies") val proficiencies: List<ApiReferenceDto> = emptyList(),
    @SerialName("proficiency_choices") val proficiencyChoices: ChoiceDto? = null,
    @SerialName("language_options") val languageOptions: ChoiceDto? = null,
    @SerialName("trait_specific") val traitSpecific: TraitSpecificDto? = null,
    @SerialName("url") val url: String = "",
)

@Serializable
data class TraitSpecificDto(
    @SerialName("damage_type") val damageType: ApiReferenceDto? = null,
    @SerialName("breath_weapon") val breathWeapon: BreathWeaponDto? = null,
    @SerialName("subtrait_options") val subtraitOptions: ChoiceDto? = null,
    @SerialName("spell_options") val spellOptions: ChoiceDto? = null,
)

@Serializable
data class BreathWeaponDto(
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("area_of_effect") val areaOfEffect: AreaOfEffectDto = AreaOfEffectDto(),
    @SerialName("damage") val damage: List<BreathWeaponDamageDto> = emptyList(),
    @SerialName("dc") val dc: DcDto = DcDto(),
    @SerialName("usage") val usage: TraitUsageDto = TraitUsageDto(),
)

@Serializable
data class BreathWeaponDamageDto(
    @SerialName("damage_at_character_level") val damageAtCharacterLevel: Map<String, String> = emptyMap(),
    @SerialName("damage_type") val damageType: ApiReferenceDto = ApiReferenceDto(),
)

@Serializable
data class TraitUsageDto(
    @SerialName("times") val times: Int = 1,
    @SerialName("type") val type: String = "per day",
)
