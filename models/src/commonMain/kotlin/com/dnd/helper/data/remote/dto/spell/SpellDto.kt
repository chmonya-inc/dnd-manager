package com.dnd.helper.data.remote.dto.spell

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.AreaOfEffectDto
import com.dnd.helper.data.remote.dto.common.DcDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpellDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("higher_level") val higherLevel: List<String> = emptyList(),
    @SerialName("range") val range: String = "",
    @SerialName("components") val components: List<String> = emptyList(), // "V" | "S" | "M"
    @SerialName("material") val material: String? = null,
    @SerialName("area_of_effect") val areaOfEffect: AreaOfEffectDto? = null,
    @SerialName("ritual") val ritual: Boolean = false,
    @SerialName("duration") val duration: String = "",
    @SerialName("concentration") val concentration: Boolean = false,
    @SerialName("casting_time") val castingTime: String = "",
    @SerialName("level") val level: Int = 0,
    @SerialName("attack_type") val attackType: String? = null,
    @SerialName("damage") val damage: SpellDamageDto? = null,
    @SerialName("dc") val dc: DcDto? = null,
    @SerialName("school") val school: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("classes") val classes: List<ApiReferenceDto> = emptyList(),
    @SerialName("subclasses") val subclasses: List<ApiReferenceDto> = emptyList(),
    @SerialName("url") val url: String = "",
)

/**
 * Exactly one of [damageAtCharacterLevel] or [damageAtSlotLevel] will be present,
 * keyed by level number (as String) mapping to dice expression (e.g. "2d6").
 */
@Serializable
data class SpellDamageDto(
    @SerialName("damage_at_character_level") val damageAtCharacterLevel: Map<String, String>? = null,
    @SerialName("damage_at_slot_level") val damageAtSlotLevel: Map<String, String>? = null,
    @SerialName("damage_type") val damageType: ApiReferenceDto? = null,
)

@Serializable
data class MagicSchoolDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("url") val url: String = "",
)
