package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import com.dnd.helper.data.remote.dto.common.PrerequisiteDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClassDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("hit_die") val hitDie: Int = 0,
    @SerialName("class_levels") val classLevels: String = "", // URL to levels list
    @SerialName("multi_classing") val multiClassing: MulticlassingDto = MulticlassingDto(),
    @SerialName("spellcasting") val spellcasting: ClassSpellcastingDto? = null,
    @SerialName("spells") val spells: String? = null, // URL to spell list
    @SerialName("starting_equipment") val startingEquipment: List<StartingEquipmentDto> = emptyList(),
    @SerialName("starting_equipment_options") val startingEquipmentOptions: List<ChoiceDto> = emptyList(),
    @SerialName("proficiency_choices") val proficiencyChoices: List<ChoiceDto> = emptyList(),
    @SerialName("proficiencies") val proficiencies: List<ApiReferenceDto> = emptyList(),
    @SerialName("saving_throws") val savingThrows: List<ApiReferenceDto> = emptyList(),
    @SerialName("subclasses") val subclasses: List<ApiReferenceDto> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class MulticlassingDto(
    @SerialName("prerequisites") val prerequisites: List<PrerequisiteDto> = emptyList(),
    @SerialName("prerequisite_options") val prerequisiteOptions: ChoiceDto? = null,
    @SerialName("proficiencies") val proficiencies: List<ApiReferenceDto> = emptyList(),
    @SerialName("proficiency_choices") val proficiencyChoices: List<ChoiceDto> = emptyList(),
)

@Serializable
data class ClassSpellcastingDto(
    @SerialName("level") val level: Int = 1,
    @SerialName("spellcasting_ability") val spellcastingAbility: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("info") val info: List<SpellcastingInfoDto> = emptyList(),
)

@Serializable
data class SpellcastingInfoDto(
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
)
