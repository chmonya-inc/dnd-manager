package com.dnd.helper.presentation.start

sealed interface StartEvent {
    data class CharacterIdChanged(val id: String) : StartEvent
    data object LoadCharacter : StartEvent
}
