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
}
