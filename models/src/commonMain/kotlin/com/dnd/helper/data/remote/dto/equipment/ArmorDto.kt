package com.dnd.helper.data.remote.dto.equipment

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.CostDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArmorDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("equipment_category") val equipmentCategory: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("armor_category") val armorCategory: String = "", // "Light" | "Medium" | "Heavy" | "Shield"
    @SerialName("armor_class") val armorClass: ArmorClassDto = ArmorClassDto(),
    @SerialName("str_minimum") val strMinimum: Int = 0,
    @SerialName("stealth_disadvantage") val stealthDisadvantage: Boolean = false,
    @SerialName("cost") val cost: CostDto = CostDto(),
    @SerialName("weight") val weight: Double? = null,
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class ArmorClassDto(
    @SerialName("base") val base: Int = 10,
    @SerialName("dex_bonus") val dexBonus: Boolean = false,
    @SerialName("max_bonus") val maxBonus: Int? = null,
)
