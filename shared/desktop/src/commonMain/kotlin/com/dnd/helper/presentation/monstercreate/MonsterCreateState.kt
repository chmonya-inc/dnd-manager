package com.dnd.helper.presentation.monstercreate

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.monster.MonsterActionDto
import com.dnd.helper.data.remote.dto.monster.MonsterProficiencyDto
import com.dnd.helper.data.remote.dto.monster.MonsterSpecialAbilityDto

data class MonsterCreateState(
    val name: String = "",
    val description: String = "",
    val challengeRating: String = "",
    val type: String = "",
    val alignment: String = "",
    val size: String = "",
    val maxHp: String = "",
    val armorClass: String = "",
    val speed: String = "",
    val hitDice: String = "",

    val strength: String = "",
    val dexterity: String = "",
    val constitution: String = "",
    val intelligence: String = "",
    val wisdom: String = "",
    val charisma: String = "",

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
