package com.dnd.helper.data.remote.dto.character

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ClassLevelDto(
    @SerialName("index") val index: String = "",
    @SerialName("url") val url: String = "",
    @SerialName("level") val level: Int = 0,
    @SerialName("ability_score_bonuses") val abilityScoreBonuses: Int = 0,
    @SerialName("prof_bonus") val profBonus: Int = 0,
    @SerialName("features") val features: List<ApiReferenceDto> = emptyList(),
    @SerialName("spellcasting") val spellcasting: ClassLevelSpellcastingDto? = null,
    @SerialName("class_specific") val classSpecific: JsonObject? = null, // Varies by class — parsed on demand
    @SerialName("class") val `class`: ApiReferenceDto? = null,
    @SerialName("subclass") val subclass: ApiReferenceDto? = null,
)

@Serializable
data class ClassLevelSpellcastingDto(
    @SerialName("cantrips_known") val cantripsKnown: Int? = null,
    @SerialName("spells_known") val spellsKnown: Int? = null,
    @SerialName("spell_slots_level_1") val spellSlotsLevel1: Int? = null,
    @SerialName("spell_slots_level_2") val spellSlotsLevel2: Int? = null,
    @SerialName("spell_slots_level_3") val spellSlotsLevel3: Int? = null,
    @SerialName("spell_slots_level_4") val spellSlotsLevel4: Int? = null,
    @SerialName("spell_slots_level_5") val spellSlotsLevel5: Int? = null,
    @SerialName("spell_slots_level_6") val spellSlotsLevel6: Int? = null,
    @SerialName("spell_slots_level_7") val spellSlotsLevel7: Int? = null,
    @SerialName("spell_slots_level_8") val spellSlotsLevel8: Int? = null,
    @SerialName("spell_slots_level_9") val spellSlotsLevel9: Int? = null,
)
