package com.dnd.helper.data.remote.dto.monster

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.ChoiceDto
import com.dnd.helper.data.remote.dto.common.DamageDto
import com.dnd.helper.data.remote.dto.common.DcDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MonsterDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("image") val image: String? = null,
    // Ability scores (flat int fields)
    @SerialName("strength") val strength: Int = 10,
    @SerialName("dexterity") val dexterity: Int = 10,
    @SerialName("constitution") val constitution: Int = 10,
    @SerialName("intelligence") val intelligence: Int = 10,
    @SerialName("wisdom") val wisdom: Int = 10,
    @SerialName("charisma") val charisma: Int = 10,
    // Core stats
    @SerialName("size") val size: String = "Medium", // "Tiny"|"Small"|"Medium"|"Large"|"Huge"|"Gargantuan"
    @SerialName("type") val type: String = "",
    @SerialName("subtype") val subtype: String? = null,
    @SerialName("alignment") val alignment: String = "",
    @SerialName("armor_class") val armorClass: List<MonsterArmorClassDto> = emptyList(),
    @SerialName("hit_points") val hitPoints: Int = 0,
    @SerialName("hit_dice") val hitDice: String = "",
    @SerialName("hit_points_roll") val hitPointsRoll: String = "",
    @SerialName("speed") val speed: MonsterSpeedDto = MonsterSpeedDto(),
    @SerialName("proficiencies") val proficiencies: List<MonsterProficiencyDto> = emptyList(),
    @SerialName("damage_vulnerabilities") val damageVulnerabilities: List<String> = emptyList(),
    @SerialName("damage_resistances") val damageResistances: List<String> = emptyList(),
    @SerialName("damage_immunities") val damageImmunities: List<String> = emptyList(),
    @SerialName("condition_immunities") val conditionImmunities: List<ApiReferenceDto> = emptyList(),
    @SerialName("senses") val senses: MonsterSenseDto = MonsterSenseDto(),
    @SerialName("languages") val languages: String = "",
    @SerialName("challenge_rating") val challengeRating: Double = 0.0,
    @SerialName("proficiency_bonus") val proficiencyBonus: Int = 2,
    @SerialName("xp") val xp: Int = 0,
    @SerialName("actions") val actions: List<MonsterActionDto> = emptyList(),
    @SerialName("legendary_actions") val legendaryActions: List<MonsterActionDto> = emptyList(),
    @SerialName("reactions") val reactions: List<MonsterActionDto> = emptyList(),
    @SerialName("special_abilities") val specialAbilities: List<MonsterSpecialAbilityDto> = emptyList(),
    @SerialName("forms") val forms: List<ApiReferenceDto> = emptyList(),
    @SerialName("url") val url: String = "",
)

@Serializable
data class MonsterArmorClassDto(
    @SerialName("type") val type: String = "", // "dex" | "natural" | "armor" | "spell" | "condition"
    @SerialName("value") val value: Int = 10,
    @SerialName("desc") val desc: String? = null,
    @SerialName("armor") val armor: List<ApiReferenceDto>? = null,
    @SerialName("spell") val spell: ApiReferenceDto? = null,
    @SerialName("condition") val condition: ApiReferenceDto? = null,
)

@Serializable
data class MonsterSpeedDto(
    @SerialName("walk") val walk: String? = null,
    @SerialName("burrow") val burrow: String? = null,
    @SerialName("climb") val climb: String? = null,
    @SerialName("fly") val fly: String? = null,
    @SerialName("swim") val swim: String? = null,
    @SerialName("hover") val hover: Boolean? = null,
)

@Serializable
data class MonsterSenseDto(
    @SerialName("passive_perception") val passivePerception: Int = 10,
    @SerialName("blindsight") val blindsight: String? = null,
    @SerialName("darkvision") val darkvision: String? = null,
    @SerialName("tremorsense") val tremorsense: String? = null,
    @SerialName("truesight") val truesight: String? = null,
)

@Serializable
data class MonsterProficiencyDto(
    @SerialName("value") val value: Int = 0,
    @SerialName("proficiency") val proficiency: ApiReferenceDto = ApiReferenceDto(),
)

@Serializable
data class MonsterActionDto(
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("attack_bonus") val attackBonus: Int? = null,
    @SerialName("dc") val dc: DcDto? = null,
    @SerialName("damage") val damage: List<DamageDto> = emptyList(),
    @SerialName("attacks") val attacks: List<MonsterAttackDto> = emptyList(),
    @SerialName("actions") val actions: List<MonsterMultiAttackActionDto> = emptyList(),
    @SerialName("options") val options: ChoiceDto? = null,
    @SerialName("action_options") val actionOptions: ChoiceDto? = null,
    @SerialName("multiattack_type") val multiattackType: String? = null,
    @SerialName("usage") val usage: MonsterUsageDto? = null,
)

@Serializable
data class MonsterAttackDto(
    @SerialName("name") val name: String = "",
    @SerialName("dc") val dc: DcDto? = null,
    @SerialName("damage") val damage: List<DamageDto> = emptyList(),
)

@Serializable
data class MonsterMultiAttackActionDto(
    @SerialName("action_name") val actionName: String = "",
    @SerialName("count") val count: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("type") val type: String = "", // "melee" | "ranged" | "ability" | "magic"
)

@Serializable
data class MonsterSpecialAbilityDto(
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: String = "",
    @SerialName("attack_bonus") val attackBonus: Int? = null,
    @SerialName("damage") val damage: List<DamageDto> = emptyList(),
    @SerialName("dc") val dc: DcDto? = null,
    @SerialName("spellcasting") val spellcasting: MonsterSpellcastingDto? = null,
    @SerialName("usage") val usage: MonsterUsageDto? = null,
)

@Serializable
data class MonsterUsageDto(
    @SerialName("type") val type: String = "", // "at will" | "per day" | "recharge after rest" | "recharge on roll"
    @SerialName("times") val times: Int? = null,
    @SerialName("rest_types") val restTypes: List<String> = emptyList(),
    @SerialName("dice") val dice: String? = null,
    @SerialName("min_value") val minValue: Int? = null,
)

@Serializable
data class MonsterSpellcastingDto(
    @SerialName("ability") val ability: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("dc") val dc: Int? = null,
    @SerialName("modifier") val modifier: Int? = null,
    @SerialName("components_required") val componentsRequired: List<String> = emptyList(),
    @SerialName("school") val school: String? = null,
    @SerialName("slots") val slots: Map<String, Int> = emptyMap(),
    @SerialName("spells") val spells: List<MonsterSpellDto> = emptyList(),
)

@Serializable
data class MonsterSpellDto(
    @SerialName("name") val name: String = "",
    @SerialName("level") val level: Int = 0,
    @SerialName("url") val url: String = "",
    @SerialName("usage") val usage: MonsterUsageDto? = null,
)
