package com.dnd.helper.presentation.characterdetail

sealed interface CharacterDetailEvent {
    data object Refresh : CharacterDetailEvent
}
