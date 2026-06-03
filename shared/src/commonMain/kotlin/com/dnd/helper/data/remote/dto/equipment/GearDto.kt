package com.dnd.helper.data.remote.dto.equipment

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.CostDto
import kotlinx.serialization.Serializable

@Serializable
data class GearDto(
    val index: String = "",
    val name: String = "",
    val equipment_category: ApiReferenceDto = ApiReferenceDto(),
    val gear_category: ApiReferenceDto = ApiReferenceDto(),
    val cost: CostDto = CostDto(),
    val weight: Double? = null,
    val image: String? = null,
    val desc: List<String> = emptyList(),
    val url: String = "",
)

@Serializable
data class EquipmentPackDto(
    val index: String = "",
    val name: String = "",
    val equipment_category: ApiReferenceDto = ApiReferenceDto(),
    val gear_category: ApiReferenceDto? = null,
    val cost: CostDto = CostDto(),
    val contents: List<ApiReferenceDto> = emptyList(),
    val image: String? = null,
    val desc: List<String> = emptyList(),
    val url: String = "",
)

@Serializable
data class EquipmentCategoryDto(
    val index: String = "",
    val name: String = "",
    val equipment: List<ApiReferenceDto> = emptyList(),
    val image: String? = null,
    val url: String = "",
)
