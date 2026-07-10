package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterProficiencies(
    val savingThrows: List<String> = emptyList(), // "strength", "dexterity", ...
    val skills: List<String> = emptyList(), // "athletics", "perception", ...
    val armor: List<String> = emptyList(), // "light", "medium", "heavy", "shield"
    val weapons: List<String> = emptyList(), // "simple", "martial"
    val tools: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
)
