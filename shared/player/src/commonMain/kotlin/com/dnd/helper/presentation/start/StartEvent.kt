package com.dnd.helper.presentation.start

sealed interface StartEvent {
    data class CharacterIdChanged(val id: String) : StartEvent
    data class TableIdChanged(val id: String) : StartEvent
    data object LoadCharacter : StartEvent
    data class LoadMyCharacter(val characterId: String, val sessionId: String) : StartEvent
    data object RefreshMyCharacters : StartEvent
    data object Logout : StartEvent
    data object LoadPendingAssignments : StartEvent
    data class RespondToAssignment(val assignmentId: String, val accept: Boolean) : StartEvent

    /** Player links their character to a campaign by gameId (sessionId). */
    data class JoinCampaign(val characterId: String, val gameId: String) : StartEvent
    data class DeleteCharacter(val characterId: String) : StartEvent
    data object DismissJoinError : StartEvent
}
