package com.dnd.helper.data.remote.dto.common

import kotlinx.serialization.Serializable

/**
 * Represents a choice a player makes (e.g., pick 2 skills from a list).
 * The 'from' field is an [OptionSetDto] that contains the available options.
 */
@Serializable
data class ChoiceDto(
    val desc: String? = null,
    val choose: Int = 1,
    val type: String = "",
    val from: OptionSetDto = OptionSetDto(),
)

/**
 * An option set defines the pool of options for a [ChoiceDto].
 * Discriminated by [option_set_type]:
 * - "options_array"      → use [options_array]
 * - "equipment_category" → use [equipment_category]
 * - "resource_list"      → use [resource_list_url]
 */
@Serializable
data class OptionSetDto(
    val option_set_type: String = "options_array",
    val options_array: List<OptionDto>? = null,
    val equipment_category: ApiReferenceDto? = null,
    val resource_list_url: String? = null,
)

/**
 * A single option within an [OptionSetDto].
 * The active fields depend on [option_type]:
 * - "reference"          → [item]
 * - "action"             → [action_name], [count], [type]
 * - "multiple"           → [items]
 * - "choice"             → [choice]
 * - "string"             → [string]
 * - "ideal"              → [desc], [alignments]
 * - "counted_reference"  → [count], [of]
 * - "score_prerequisite" → [ability_score], [minimum_score]
 * - "ability_bonus"      → [ability_score], [bonus]
 * - "breath"             → [name], [dc], [damage]
 * - "damage"             → [damage_type], [damage_dice], [notes]
 */
@Serializable
data class OptionDto(
    val option_type: String = "",
    // reference
    val item: ApiReferenceDto? = null,
    // action
    val action_name: String? = null,
    val count: Double? = null,
    val type: String? = null, // "melee" | "ranged" | "ability" | "magic"
    // multiple
    val items: List<OptionDto>? = null,
    // choice (nested)
    val choice: ChoiceDto? = null,
    // string
    val string: String? = null,
    // ideal
    val desc: String? = null,
    val alignments: List<ApiReferenceDto>? = null,
    // counted_reference
    val of: ApiReferenceDto? = null,
    // score_prerequisite / ability_bonus
    val ability_score: ApiReferenceDto? = null,
    val minimum_score: Double? = null,
    val bonus: Double? = null,
    // breath
    val name: String? = null,
    val dc: DcDto? = null,
    val damage: List<DamageDto>? = null,
    // damage option
    val damage_type: ApiReferenceDto? = null,
    val damage_dice: String? = null,
    val notes: String? = null,
)
