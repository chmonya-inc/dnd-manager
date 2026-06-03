package com.dnd.helper.data.remote.dto.monster

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import com.dnd.helper.data.remote.dto.common.DamageDto
import com.dnd.helper.data.remote.dto.common.DcDto
import kotlinx.serialization.Serializable

@Serializable
data class MonsterDto(
    val index: String = "",
    val name: String = "",
    val desc: List<String> = emptyList(),
    val image: String? = null,
    // Ability scores (flat int fields)
    val strength: Int = 10,
    val dexterity: Int = 10,
    val constitution: Int = 10,
    val intelligence: Int = 10,
    val wisdom: Int = 10,
    val charisma: Int = 10,
    // Core stats
    val size: String = "Medium", // "Tiny"|"Small"|"Medium"|"Large"|"Huge"|"Gargantuan"
    val type: String = "",
    val subtype: String? = null,
    val alignment: String = "",
    val armor_class: List<MonsterArmorClassDto> = emptyList(),
    val hit_points: Int = 0,
    val hit_dice: String = "",
    val hit_points_roll: String = "",
    val speed: MonsterSpeedDto = MonsterSpeedDto(),
    val proficiencies: List<MonsterProficiencyDto> = emptyList(),
    val damage_vulnerabilities: List<String> = emptyList(),
    val damage_resistances: List<String> = emptyList(),
    val damage_immunities: List<String> = emptyList(),
    val condition_immunities: List<ApiReferenceDto> = emptyList(),
    val senses: MonsterSenseDto = MonsterSenseDto(),
    val languages: String = "",
    val challenge_rating: Double = 0.0,
    val proficiency_bonus: Int = 2,
    val xp: Int = 0,
    val actions: List<MonsterActionDto> = emptyList(),
    val legendary_actions: List<MonsterActionDto> = emptyList(),
    val reactions: List<MonsterActionDto> = emptyList(),
    val special_abilities: List<MonsterSpecialAbilityDto> = emptyList(),
    val forms: List<ApiReferenceDto> = emptyList(),
    val url: String = "",
)

@Serializable
data class MonsterArmorClassDto(
    val type: String = "",  // "dex" | "natural" | "armor" | "spell" | "condition"
    val value: Int = 10,
    val desc: String? = null,
    val armor: List<ApiReferenceDto>? = null,
    val spell: ApiReferenceDto? = null,
    val condition: ApiReferenceDto? = null,
)

@Serializable
data class MonsterSpeedDto(
    val walk: String? = null,
    val burrow: String? = null,
    val climb: String? = null,
    val fly: String? = null,
    val swim: String? = null,
    val hover: Boolean? = null,
)

@Serializable
data class MonsterSenseDto(
    val passive_perception: Int = 10,
    val blindsight: String? = null,
    val darkvision: String? = null,
    val tremorsense: String? = null,
    val truesight: String? = null,
)

@Serializable
data class MonsterProficiencyDto(
    val value: Int = 0,
    val proficiency: ApiReferenceDto = ApiReferenceDto(),
)

@Serializable
data class MonsterActionDto(
    val name: String = "",
    val desc: String = "",
    val attack_bonus: Int? = null,
    val dc: DcDto? = null,
    val damage: List<DamageDto> = emptyList(),
    val attacks: List<MonsterAttackDto> = emptyList(),
    val actions: List<MonsterMultiAttackActionDto> = emptyList(),
    val options: ChoiceDto? = null,
    val action_options: ChoiceDto? = null,
    val multiattack_type: String? = null,
    val usage: MonsterUsageDto? = null,
)

@Serializable
data class MonsterAttackDto(
    val name: String = "",
    val dc: DcDto? = null,
    val damage: DamageDto? = null,
)

@Serializable
data class MonsterMultiAttackActionDto(
    val action_name: String = "",
    val count: Double = 1.0,
    val type: String = "", // "melee" | "ranged" | "ability" | "magic"
)

@Serializable
data class MonsterSpecialAbilityDto(
    val name: String = "",
    val desc: String = "",
    val attack_bonus: Int? = null,
    val damage: List<DamageDto> = emptyList(),
    val dc: DcDto? = null,
    val spellcasting: MonsterSpellcastingDto? = null,
    val usage: MonsterUsageDto? = null,
)

@Serializable
data class MonsterUsageDto(
    val type: String = "",  // "at will" | "per day" | "recharge after rest" | "recharge on roll"
    val times: Int? = null,
    val rest_types: List<String> = emptyList(),
    val dice: String? = null,
    val min_value: Int? = null,
)

@Serializable
data class MonsterSpellcastingDto(
    val ability: ApiReferenceDto = ApiReferenceDto(),
    val dc: Int? = null,
    val modifier: Int? = null,
    val components_required: List<String> = emptyList(),
    val school: String? = null,
    val slots: Map<String, Int> = emptyMap(),
    val spells: List<MonsterSpellDto> = emptyList(),
)

@Serializable
data class MonsterSpellDto(
    val name: String = "",
    val level: Int = 0,
    val url: String = "",
    val usage: MonsterUsageDto? = null,
)
