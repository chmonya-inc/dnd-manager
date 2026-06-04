package com.dnd.helper.data.remote.dto.spell

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.AreaOfEffectDto
import com.dnd.helper.data.remote.dto.common.DcDto
import kotlinx.serialization.Serializable

@Serializable
data class SpellDto(
    val index: String = "",
    val name: String = "",
    val desc: List<String> = emptyList(),
    val higher_level: List<String> = emptyList(),
    val range: String = "",
    val components: List<String> = emptyList(), // "V" | "S" | "M"
    val material: String? = null,
    val area_of_effect: AreaOfEffectDto? = null,
    val ritual: Boolean = false,
    val duration: String = "",
    val concentration: Boolean = false,
    val casting_time: String = "",
    val level: Int = 0,
    val attack_type: String? = null,
    val damage: SpellDamageDto? = null,
    val dc: DcDto? = null,
    val school: ApiReferenceDto = ApiReferenceDto(),
    val classes: List<ApiReferenceDto> = emptyList(),
    val subclasses: List<ApiReferenceDto> = emptyList(),
    val url: String = "",
)

/**
 * Exactly one of [damage_at_character_level] or [damage_at_slot_level] will be present,
 * keyed by level number (as String) mapping to dice expression (e.g. "2d6").
 */
@Serializable
data class SpellDamageDto(
    val damage_at_character_level: Map<String, String>? = null,
    val damage_at_slot_level: Map<String, String>? = null,
    val damage_type: ApiReferenceDto? = null,
)

@Serializable
data class MagicSchoolDto(
    val index: String = "",
    val name: String = "",
    val desc: String = "",
    val url: String = "",
)
