package com.dnd.helper.presentation.charactercreate

import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.Spell
import com.dnd.helper.domain.model.Weapon

data class CharacterCreateState(
    // Basic info
    val name: String = "",
    val playerName: String = "",
    val race: String = "",
    val subrace: String = "",
    val characterClass: String = "",
    val subclass: String = "",
    val background: String = "",
    val alignment: String = "",
    val level: String = "1",
    val experiencePoints: String = "0",
    val description: String = "",
    val imageUrl: String = "",
    val aiPrompt: String = "",

    val availableClasses: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableSubclasses: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableRaces: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableSubraces: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableBackgrounds: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableAlignments: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableLanguages: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableSkills: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableEquipment: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableFeats: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableFeatures: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableTraits: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),
    val availableSpells: List<com.dnd.helper.data.remote.dto.common.ApiReferenceDto> = emptyList(),

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
    val armorProficiencies: String = "",

    // Multi-select Lists
    val selectedSkills: List<String> = emptyList(),
    val selectedWeapons: List<String> = emptyList(),
    val selectedTools: List<String> = emptyList(),
    val selectedLanguages: List<String> = emptyList(),
    val selectedClassFeatures: List<String> = emptyList(),
    val selectedRacialTraits: List<String> = emptyList(),
    val selectedFeats: List<String> = emptyList(),

    // Lists
    val items: List<Item> = emptyList(),
    val weapons: List<Weapon> = emptyList(),
    val spellList: List<Spell> = emptyList(),
    val notes: List<com.dnd.helper.domain.model.Note> = emptyList(),

    // Features (newline-separated)
    val classFeatures: String = "",
    val racialTraits: String = "",
    val feats: String = "",

    // UI state
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val aiWidth: Int = 1024,
    val aiHeight: Int = 1024,
)
