package com.dnd.helper.data.remote.dto.equipment

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.CostDto
import com.dnd.helper.data.remote.dto.common.DamageDto
import kotlinx.serialization.Serializable

@Serializable
data class WeaponDto(
    val index: String = "",
    val name: String = "",
    val equipment_category: ApiReferenceDto = ApiReferenceDto(),
    val weapon_category: String = "",     // "Simple" | "Martial"
    val weapon_range: String = "",        // "Melee" | "Ranged"
    val category_range: String = "",
    val range: WeaponRangeDto = WeaponRangeDto(),
    val damage: DamageDto? = null,
    val two_handed_damage: DamageDto? = null,
    val properties: List<ApiReferenceDto> = emptyList(),
    val cost: CostDto = CostDto(),
    val weight: Double? = null,
    val desc: List<String> = emptyList(),
    val special: List<String> = emptyList(),
    val url: String = "",
)

@Serializable
data class WeaponRangeDto(
    val normal: Double = 5.0,
    val long: Double? = null,
)
