package com.dnd.helper.presentation.charactercreate

import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.Skill
import com.dnd.helper.domain.model.Weapon

data class CharacterCreateState(
    // Basic info
    val name: String = "",
    val playerName: String = "",
    val race: String = "",
    val characterClass: String = "",
    val subclass: String = "",
    val background: String = "",
    val level: String = "1",
    val experiencePoints: String = "0",
    val description: String = "",
    val imageUrl: String = "",
    val aiPrompt: String = "",

    // Appearance
    val age: String = "",
    val gender: String = "",
    val height: String = "",
    val weight: String = "",
    val eyes: String = "",
    val hair: String = "",
    val skin: String = "",

    // Ability Scores
    val strength: String = "10",
    val dexterity: String = "10",
    val constitution: String = "10",
    val intelligence: String = "10",
    val wisdom: String = "10",
    val charisma: String = "10",

    // HP & Combat
    val maxHp: String = "10",
    val currentHp: String = "10",
    val tempHp: String = "0",
    val armorClass: String = "10",
    val initiative: String = "0",
    val speed: String = "30",
    val proficiencyBonus: String = "2",
    val hitDice: String = "1d8",
    val hitDiceCurrent: String = "1",

    // Status
    val inspiration: Boolean = false,
    val exhaustion: String = "0",
    val conditions: String = "",
    val deathSaveSuccesses: String = "0",
    val deathSaveFailures: String = "0",

    // Proficiencies (comma-separated)
    val savingThrows: String = "",
    val skills: String = "",
    val armorProficiencies: String = "",
    val weaponProficiencies: String = "",
    val toolProficiencies: String = "",
    val languages: String = "",

    // Lists
    val items: List<Item> = emptyList(),
    val weapons: List<Weapon> = emptyList(),
    val skillList: List<Skill> = emptyList(),
    val notes: List<com.dnd.helper.domain.model.Note> = emptyList(),

    // Features (newline-separated)
    val classFeatures: String = "",
    val racialTraits: String = "",
    val feats: String = "",

    // UI state
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val aiWidth: Int = 512,
    val aiHeight: Int = 512,
)
