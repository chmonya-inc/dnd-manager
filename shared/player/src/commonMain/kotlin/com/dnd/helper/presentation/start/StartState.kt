package com.dnd.helper.presentation.start

import com.dnd.helper.data.remote.dto.auth.PendingAssignmentDto

data class StartState(
    val characterId: String = "",
    val tableId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val characterTemplates: List<com.dnd.helper.data.remote.dto.auth.CharacterTemplateDto> = emptyList(),
    val standaloneInstances: List<com.dnd.helper.data.remote.dto.auth.MyCharacterDto> = emptyList(),
    val isLoadingMyCharacters: Boolean = false,
    val username: String? = null,
    val isMaster: Boolean = false,
    val pendingAssignments: List<PendingAssignmentDto> = emptyList(),
    val isLoadingAssignments: Boolean = false,
    val assignmentError: String? = null,
    val isJoiningCampaign: Boolean = false,
    val joinError: String? = null,
)
