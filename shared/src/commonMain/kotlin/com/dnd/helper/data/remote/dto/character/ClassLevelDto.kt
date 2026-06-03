package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ClassLevelDto(
    val index: String = "",
    val url: String = "",
    val level: Int = 0,
    val ability_score_bonuses: Int = 0,
    val prof_bonus: Int = 0,
    val features: List<ApiReferenceDto> = emptyList(),
    val spellcasting: ClassLevelSpellcastingDto? = null,
    val class_specific: JsonObject? = null, // Varies by class — parsed on demand
    val `class`: ApiReferenceDto? = null,
    val subclass: ApiReferenceDto? = null,
)

@Serializable
data class ClassLevelSpellcastingDto(
    val cantrips_known: Int? = null,
    val spells_known: Int? = null,
    val spell_slots_level_1: Int? = null,
    val spell_slots_level_2: Int? = null,
    val spell_slots_level_3: Int? = null,
    val spell_slots_level_4: Int? = null,
    val spell_slots_level_5: Int? = null,
    val spell_slots_level_6: Int? = null,
    val spell_slots_level_7: Int? = null,
    val spell_slots_level_8: Int? = null,
    val spell_slots_level_9: Int? = null,
)
