package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.RemoteDataSource
import com.dnd.helper.data.remote.dto.auth.AssignmentStatusDto
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssignCharacterState(
    val username: String = "",
    val isAssigning: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val assignmentStatuses: List<AssignmentStatusDto> = emptyList(),
    val isLoadingStatuses: Boolean = false
)

class AssignCharacterViewModel(
    private val remoteDataSource: RemoteDataSource,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AssignCharacterState())
    val state = _state.asStateFlow()

    /** Tracks the current session for WebSocket listening */
    private var activeSessionId: String? = null

    init {
        observeAssignmentUpdates()
    }

    /**
     * Listen for WebSocket updates about assignment responses.
     * When a player accepts or revokes, refresh the status list.
     */
    private fun observeAssignmentUpdates() {
        viewModelScope.launch {
            characterRepository.remoteUpdates.collect { updateMessage ->
                val parts = updateMessage.split(":")
                val updateType = parts.firstOrNull() ?: return@collect

                if (updateType == "assignment_accepted" || updateType == "assignment_revoked") {
                    activeSessionId?.let { sid ->
                        loadAssignmentStatuses(sid)
                    }
                }
            }
        }
    }

    fun onUsernameChanged(username: String) {
        _state.value = _state.value.copy(username = username, error = null, success = false)
    }

    fun assignCharacter(characterId: String, sessionId: String) {
        // Re-entry guard: the dialog button can't disable synchronously (Swing's Main dispatcher
        // is not immediate, so the launched coroutine runs on a later EDT tick). A double-click
        // would otherwise fire two requests and create two pending assignments.
        if (_state.value.isAssigning) return
        activeSessionId = sessionId
        val username = _state.value.username.trim()
        if (username.isEmpty()) {
            _state.value = _state.value.copy(error = "Username cannot be empty")
            return
        }

        // Flip isAssigning synchronously (this EDT tick) so the button disables on this click.
        _state.value = _state.value.copy(isAssigning = true, error = null, success = false)
        viewModelScope.launch {
            // Use the new assignment flow: sends a request to the player
            when (val res = remoteDataSource.createAssignment(characterId, sessionId, username)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isAssigning = false,
                        success = true
                    )
                    loadAssignmentStatuses(sessionId)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isAssigning = false,
                        error = res.error.toUserMessage()
                    )
                }
            }
        }
    }

    fun unassignCharacter(characterId: String, sessionId: String) {
        if (_state.value.isAssigning) return
        activeSessionId = sessionId
        _state.value = _state.value.copy(isAssigning = true, error = null, success = false)
        viewModelScope.launch {
            when (val res = remoteDataSource.assignCharacterByUsername(characterId, sessionId, null)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isAssigning = false,
                        success = true,
                        username = ""
                    )
                    loadAssignmentStatuses(sessionId)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isAssigning = false,
                        error = res.error.toUserMessage()
                    )
                }
            }
        }
    }

    fun loadAssignmentStatuses(sessionId: String) {
        activeSessionId = sessionId
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingStatuses = true)
            when (val res = remoteDataSource.getAssignmentStatuses(sessionId)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        assignmentStatuses = res.data,
                        isLoadingStatuses = false
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoadingStatuses = false)
                }
            }
        }
    }
}
