package com.dnd.helper.presentation.characterdetail

import com.dnd.helper.domain.model.Character

sealed interface CharacterDetailEvent {
    data object Refresh : CharacterDetailEvent
    data class UpdateStat(val statName: String, val delta: Int) : CharacterDetailEvent
    data class UpdateHp(val delta: Int) : CharacterDetailEvent
    data class UpdateMaxHp(val delta: Int) : CharacterDetailEvent
    data class UpdateLevel(val delta: Int) : CharacterDetailEvent
    data class ToggleItemEquipped(val itemId: String) : CharacterDetailEvent
    data object ToggleInspiration : CharacterDetailEvent
    data object RollDeathSave : CharacterDetailEvent

    // New string editing events
    data object ToggleEdit : CharacterDetailEvent
    data class EditCharacter(val character: Character) : CharacterDetailEvent
    data object SaveChanges : CharacterDetailEvent
    data object ToggleMasterMode : CharacterDetailEvent
    data object AddSkill : CharacterDetailEvent
    data class RemoveSkill(val skillId: String) : CharacterDetailEvent
    data class UpdateSkill(val skill: com.dnd.helper.domain.model.Skill) : CharacterDetailEvent
    data object AddItem : CharacterDetailEvent
    data class RemoveItem(val itemId: String) : CharacterDetailEvent
    data class UpdateItem(val item: com.dnd.helper.domain.model.Item) : CharacterDetailEvent
}
