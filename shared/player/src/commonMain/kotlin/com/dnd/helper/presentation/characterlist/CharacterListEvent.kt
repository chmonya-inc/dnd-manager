package com.dnd.helper.presentation.characterlist

sealed interface CharacterListEvent {
    data object Refresh : CharacterListEvent
    data class CharacterClicked(val characterId: String) : CharacterListEvent
}
