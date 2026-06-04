package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import com.dnd.helper.data.remote.dto.common.PrerequisiteDto
import kotlinx.serialization.Serializable

@Serializable
data class ClassDto(
    val index: String = "",
    val name: String = "",
    val hit_die: Int = 0,
    val class_levels: String = "",       // URL to levels list
    val multi_classing: MulticlassingDto = MulticlassingDto(),
    val spellcasting: ClassSpellcastingDto? = null,
    val spells: String? = null,           // URL to spell list
    val starting_equipment: List<StartingEquipmentDto> = emptyList(),
    val starting_equipment_options: List<ChoiceDto> = emptyList(),
    val proficiency_choices: List<ChoiceDto> = emptyList(),
    val proficiencies: List<ApiReferenceDto> = emptyList(),
    val saving_throws: List<ApiReferenceDto> = emptyList(),
    val subclasses: List<ApiReferenceDto> = emptyList(),
    val url: String = "",
)

@Serializable
data class MulticlassingDto(
    val prerequisites: List<PrerequisiteDto> = emptyList(),
    val prerequisite_options: ChoiceDto? = null,
    val proficiencies: List<ApiReferenceDto> = emptyList(),
    val proficiency_choices: List<ChoiceDto> = emptyList(),
)

@Serializable
data class ClassSpellcastingDto(
    val level: Int = 1,
    val spellcasting_ability: ApiReferenceDto = ApiReferenceDto(),
    val info: List<SpellcastingInfoDto> = emptyList(),
)

@Serializable
data class SpellcastingInfoDto(
    val name: String = "",
    val desc: List<String> = emptyList(),
)
