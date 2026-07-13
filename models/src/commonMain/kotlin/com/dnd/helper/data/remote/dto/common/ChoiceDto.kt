package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a choice a player makes (e.g., pick 2 skills from a list).
 * The 'from' field is an [OptionSetDto] that contains the available options.
 */
@Serializable
data class ChoiceDto(
    @SerialName("desc") val desc: String? = null,
    @SerialName("choose") val choose: Int = 1,
    @SerialName("type") val type: String = "",
    @SerialName("from") val from: OptionSetDto = OptionSetDto(),
)

/**
 * An option set defines the pool of options for a [ChoiceDto].
 * Discriminated by [optionSetType]:
 * - "options_array"      → use [optionsArray]
 * - "equipment_category" → use [equipmentCategory]
 * - "resource_list"      → use [resourceListUrl]
 */
@Serializable
data class OptionSetDto(
    @SerialName("option_set_type") val optionSetType: String = "options_array",
    @SerialName("options_array") val optionsArray: List<OptionDto>? = null,
    @SerialName("equipment_category") val equipmentCategory: ApiReferenceDto? = null,
    @SerialName("resource_list_url") val resourceListUrl: String? = null,
)

/**
 * A single option within an [OptionSetDto].
 * The active fields depend on [optionType]:
 * - "reference"          → [item]
 * - "action"             → [actionName], [count], [type]
 * - "multiple"           → [items]
 * - "choice"             → [choice]
 * - "string"             → [string]
 * - "ideal"              → [desc], [alignments]
 * - "counted_reference"  → [count], [of]
 * - "score_prerequisite" → [abilityScore], [minimumScore]
 * - "ability_bonus"      → [abilityScore], [bonus]
 * - "breath"             → [name], [dc], [damage]
 * - "damage"             → [damageType], [damageDice], [notes]
 */
@Serializable
data class OptionDto(
    @SerialName("option_type") val optionType: String = "",
    // reference
    @SerialName("item") val item: ApiReferenceDto? = null,
    // action
    @SerialName("action_name") val actionName: String? = null,
    @SerialName("count") val count: kotlinx.serialization.json.JsonElement? = null,
    @SerialName("type") val type: String? = null, // "melee" | "ranged" | "ability" | "magic"
    // multiple
    @SerialName("items") val items: List<OptionDto>? = null,
    // choice (nested)
    @SerialName("choice") val choice: ChoiceDto? = null,
    // string
    @SerialName("string") val string: String? = null,
    // ideal
    @SerialName("desc") val desc: String? = null,
    @SerialName("alignments") val alignments: List<ApiReferenceDto>? = null,
    // counted_reference
    @SerialName("of") val of: ApiReferenceDto? = null,
    // score_prerequisite / ability_bonus
    @SerialName("ability_score") val abilityScore: ApiReferenceDto? = null,
    @SerialName("minimum_score") val minimumScore: Double? = null,
    @SerialName("bonus") val bonus: Double? = null,
    // breath
    @SerialName("name") val name: String? = null,
    @SerialName("dc") val dc: DcDto? = null,
    @SerialName("damage") val damage: List<DamageDto>? = null,
    // damage option
    @SerialName("damage_type") val damageType: ApiReferenceDto? = null,
    @SerialName("damage_dice") val damageDice: String? = null,
    @SerialName("notes") val notes: String? = null,
)
