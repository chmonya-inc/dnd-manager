package com.dnd.helper.presentation.start

sealed interface StartEvent {
    data class CharacterIdChanged(val id: String) : StartEvent
    data class TableIdChanged(val id: String) : StartEvent
    data object LoadCharacter : StartEvent
    data class LoadMyCharacter(val characterId: String) : StartEvent
    data object RefreshMyCharacters : StartEvent
    data object Logout : StartEvent
}
