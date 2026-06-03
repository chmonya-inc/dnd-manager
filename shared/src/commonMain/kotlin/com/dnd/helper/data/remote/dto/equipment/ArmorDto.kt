package com.dnd.helper.data.remote.dto.equipment

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.CostDto
import kotlinx.serialization.Serializable

@Serializable
data class ArmorDto(
    val index: String = "",
    val name: String = "",
    val equipment_category: ApiReferenceDto = ApiReferenceDto(),
    val armor_category: String = "",           // "Light" | "Medium" | "Heavy" | "Shield"
    val armor_class: ArmorClassDto = ArmorClassDto(),
    val str_minimum: Int = 0,
    val stealth_disadvantage: Boolean = false,
    val cost: CostDto = CostDto(),
    val weight: Double? = null,
    val desc: List<String> = emptyList(),
    val url: String = "",
)

@Serializable
data class ArmorClassDto(
    val base: Int = 10,
    val dex_bonus: Boolean = false,
    val max_bonus: Int? = null,
)
