package com.dnd.helper.data.remote.dto.equipment

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.CostDto
import com.dnd.helper.data.remote.dto.common.DamageDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeaponDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("equipment_category") val equipmentCategory: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("weapon_category") val weaponCategory: String = "", // "Simple" | "Martial"
    @SerialName("weapon_range") val weaponRange: String = "", // "Melee" | "Ranged"
    @SerialName("category_range") val categoryRange: String = "",
    @SerialName("range") val range: WeaponRangeDto = WeaponRangeDto(),
    @SerialName("damage") val damage: DamageDto? = null,
    @SerialName("two_handed_damage") val twoHandedDamage: DamageDto? = null,
    @SerialName("properties") val properties: List<ApiReferenceDto> = emptyList(),
    @SerialName("cost") val cost: CostDto = CostDto(),
    @SerialName("weight") val weight: Double? = null,
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("special") val special: List<String> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class WeaponRangeDto(
    @SerialName("normal") val normal: Double = 5.0,
    @SerialName("long") val long: Double? = null,
)
