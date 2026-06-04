package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundDto(
    val index: String = "",
    val name: String = "",
    val starting_proficiencies: List<ApiReferenceDto> = emptyList(),
    val language_options: ChoiceDto = ChoiceDto(),
    val starting_equipment: List<StartingEquipmentDto> = emptyList(),
    val starting_equipment_options: List<ChoiceDto> = emptyList(),
    val feature: BackgroundFeatureDto = BackgroundFeatureDto(),
    val personality_traits: ChoiceDto = ChoiceDto(),
    val ideals: ChoiceDto = ChoiceDto(),
    val bonds: ChoiceDto = ChoiceDto(),
    val flaws: ChoiceDto = ChoiceDto(),
    val url: String = "",
)

@Serializable
data class StartingEquipmentDto(
    val equipment: ApiReferenceDto = ApiReferenceDto(),
    val quantity: Int = 1,
)

@Serializable
data class BackgroundFeatureDto(
    val name: String = "",
    val desc: List<String> = emptyList(),
)
