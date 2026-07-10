package com.dnd.helper.presentation.monstercreate

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.monster.MonsterActionDto
import com.dnd.helper.data.remote.dto.monster.MonsterProficiencyDto
import com.dnd.helper.data.remote.dto.monster.MonsterSpecialAbilityDto

data class MonsterCreateState(
    val name: String = "",
    val description: String = "",
    val challengeRating: String = "1",
    val type: String = "Humanoid",
    val alignment: String = "Neutral",
    val size: String = "Medium",
    val maxHp: String = "10",
    val armorClass: String = "10",
    val speed: String = "30",
    val hitDice: String = "",

    val strength: String = "10",
    val dexterity: String = "10",
    val constitution: String = "10",
    val intelligence: String = "10",
    val wisdom: String = "10",
    val charisma: String = "10",

    val selectedLanguages: List<String> = emptyList(),
    val selectedConditionImmunities: List<String> = emptyList(),
    val selectedDamageImmunities: List<String> = emptyList(),
    val selectedDamageResistances: List<String> = emptyList(),
    val selectedDamageVulnerabilities: List<String> = emptyList(),

    val specialAbilities: List<MonsterSpecialAbilityDto> = emptyList(),
    val actions: List<MonsterActionDto> = emptyList(),
    val legendaryActions: List<MonsterActionDto> = emptyList(),
    val reactions: List<MonsterActionDto> = emptyList(),
    val monsterProficiencies: List<MonsterProficiencyDto> = emptyList(),

    val availableAlignments: List<ApiReferenceDto> = emptyList(),
    val availableLanguages: List<ApiReferenceDto> = emptyList(),
    val availableConditions: List<ApiReferenceDto> = emptyList(),
    val availableDamageTypes: List<ApiReferenceDto> = emptyList(),
    val availableProficiencies: List<ApiReferenceDto> = emptyList(),

    val imageUrl: String = "",
    val aiPrompt: String = "",
    val aiWidth: Int = 1024,
    val aiHeight: Int = 1024,

    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)
