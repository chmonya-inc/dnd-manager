package com.dnd.helper.data.remote.dto.equipment

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.CostDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GearDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("equipment_category") val equipmentCategory: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("gear_category") val gearCategory: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("cost") val cost: CostDto = CostDto(),
    @SerialName("weight") val weight: Double? = null,
    @SerialName("image") val image: String? = null,
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class EquipmentPackDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("equipment_category") val equipmentCategory: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("gear_category") val gearCategory: ApiReferenceDto? = null,
    @SerialName("cost") val cost: CostDto = CostDto(),
    @SerialName("contents") val contents: List<ApiReferenceDto> = emptyList(),
    @SerialName("image") val image: String? = null,
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class EquipmentCategoryDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("equipment") val equipment: List<ApiReferenceDto> = emptyList(),
    @SerialName("image") val image: String? = null,
    @SerialName("url") val url: String = "",
)
