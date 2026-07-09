package com.dnd.helper.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.KtorRemoteDataSource
import com.dnd.helper.domain.common.IdUtils
import com.dnd.helper.domain.repository.AuthRepository
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class StartViewModel(
    private val storage: CharacterStorage,
    private val remoteDataSource: KtorRemoteDataSource,
    private val authRepository: AuthRepository,
    private val characterRepository: CharacterRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        StartState(
            characterId = storage.getCharacterId() ?: "",
            tableId = storage.getTableId() ?: "",
            username = null,
            isMaster = authRepository.getUserRole() == "MASTER"
        )
    )
    val state = _state.asStateFlow()

    init {
        loadMyCharacters()
        loadPendingAssignments()
        observeWebSocketUpdates()
        startPolling()
    }

    /**
     * Observe WebSocket updates from the current session.
     * Reacts to "characters" and "assignment" update types.
     */
    private fun observeWebSocketUpdates() {
        viewModelScope.launch {
            characterRepository.remoteUpdates.collect { updateMessage ->
                val parts = updateMessage.split(":")
                val updateType = parts.firstOrNull() ?: return@collect

                when (updateType) {
                    "characters", "assignment_accepted", "assignment_revoked" -> {
                        // Character ownership or assignment status changed
                        loadMyCharacters()
                        loadPendingAssignments()
                    }
                    "assignment" -> {
                        // New incoming assignment request
                        loadPendingAssignments()
                    }
                }
            }
        }
    }

    /**
     * Periodic polling as a fallback — ensures the player sees updates
     * even if the WebSocket isn't connected to the relevant session.
     */
    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(15_000) // every 15 seconds
                loadPendingAssignments()
                loadMyCharacters()
            }
        }
    }

    fun loadMyCharacters() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMyCharacters = true)
            when (val res = remoteDataSource.getMyCharacters()) {
                is com.dnd.helper.domain.common.Result.Success -> {
                    _state.value = _state.value.copy(
                        myCharacters = res.data,
                        isLoadingMyCharacters = false
                    )
                }
                is com.dnd.helper.domain.common.Result.Error -> {
                    _state.value = _state.value.copy(isLoadingMyCharacters = false)
                }
            }
        }
    }

    fun loadPendingAssignments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingAssignments = true)
            when (val res = remoteDataSource.getPendingAssignments()) {
                is com.dnd.helper.domain.common.Result.Success -> {
                    _state.value = _state.value.copy(
                        pendingAssignments = res.data,
                        isLoadingAssignments = false,
                        assignmentError = null
                    )
                }
                is com.dnd.helper.domain.common.Result.Error -> {
                    _state.value = _state.value.copy(isLoadingAssignments = false)
                }
            }
        }
    }

    private fun respondToAssignment(assignmentId: String, accept: Boolean) {
        viewModelScope.launch {
            when (val res = remoteDataSource.respondToAssignment(assignmentId, accept)) {
                is com.dnd.helper.domain.common.Result.Success -> {
                    // Remove from pending list
                    _state.value = _state.value.copy(
                        pendingAssignments = _state.value.pendingAssignments.filter { it.assignmentId != assignmentId }
                    )
                    // If accepted, reload my characters so the new character appears
                    if (accept) {
                        loadMyCharacters()
                    }
                }
                is com.dnd.helper.domain.common.Result.Error -> {
                    _state.value = _state.value.copy(
                        assignmentError = "Failed to respond: ${res.error}"
                    )
                }
            }
        }
    }

    fun onEvent(event: StartEvent) {
        when (event) {
            is StartEvent.CharacterIdChanged -> {
                _state.value = _state.value.copy(characterId = event.id)
            }
            is StartEvent.TableIdChanged -> {
                _state.value = _state.value.copy(tableId = event.id.trim())
            }
            is StartEvent.LoadMyCharacter -> {
                // Save session context and navigate
                val char = _state.value.myCharacters.find { it.character.id == event.characterId }
                if (char != null) {
                    storage.saveCharacterId(char.character.id)
                    storage.saveTableId(char.sessionId)
                    _state.value = _state.value.copy(
                        characterId = char.character.id,
                        tableId = char.sessionId
                    )
                }
            }
            StartEvent.LoadCharacter -> {
                if (_state.value.characterId.isNotBlank()) {
                    storage.saveCharacterId(_state.value.characterId.trim())
                }
                if (_state.value.tableId.isNotBlank()) {
                    val decodedId = IdUtils.decode(_state.value.tableId)
                    storage.saveTableId(decodedId)
                }
            }
            StartEvent.RefreshMyCharacters -> {
                loadMyCharacters()
                loadPendingAssignments()
            }
            StartEvent.Logout -> {
                viewModelScope.launch {
                    authRepository.logout()
                }
            }
            StartEvent.LoadPendingAssignments -> loadPendingAssignments()
            is StartEvent.RespondToAssignment -> respondToAssignment(event.assignmentId, event.accept)
        }
    }
}
