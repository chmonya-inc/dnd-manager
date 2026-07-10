package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BackgroundDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("starting_proficiencies") val startingProficiencies: List<ApiReferenceDto> = emptyList(),
    @SerialName("language_options") val languageOptions: ChoiceDto = ChoiceDto(),
    @SerialName("starting_equipment") val startingEquipment: List<StartingEquipmentDto> = emptyList(),
    @SerialName("starting_equipment_options") val startingEquipmentOptions: List<ChoiceDto> = emptyList(),
    @SerialName("feature") val feature: BackgroundFeatureDto = BackgroundFeatureDto(),
    @SerialName("personality_traits") val personalityTraits: ChoiceDto = ChoiceDto(),
    @SerialName("ideals") val ideals: ChoiceDto = ChoiceDto(),
    @SerialName("bonds") val bonds: ChoiceDto = ChoiceDto(),
    @SerialName("flaws") val flaws: ChoiceDto = ChoiceDto(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class StartingEquipmentDto(
    @SerialName("equipment") val equipment: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("quantity") val quantity: Int = 1,
)

@Serializable
data class BackgroundFeatureDto(
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
)
