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

    private fun observeWebSocketUpdates() {
        viewModelScope.launch {
            characterRepository.remoteUpdates.collect { updateMessage ->
                val parts = updateMessage.split(":")
                val updateType = parts.firstOrNull() ?: return@collect

                when (updateType) {
                    "characters", "assignment_accepted", "assignment_revoked" -> {
                        refreshMyCharactersSilently()
                        refreshPendingAssignmentsSilently()
                    }
                    "assignment" -> {
                        refreshPendingAssignmentsSilently()
                    }
                }
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(2_000)
                refreshPendingAssignmentsSilently()
                refreshMyCharactersSilently()
            }
        }
    }

    fun loadMyCharacters() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingMyCharacters = true)
            when (val res = remoteDataSource.getMyCharacters()) {
                is com.dnd.helper.domain.common.Result.Success -> {
                    _state.value = _state.value.copy(
                        characterTemplates = res.data.templates,
                        standaloneInstances = res.data.standaloneInstances,
                        isLoadingMyCharacters = false
                    )
                }
                is com.dnd.helper.domain.common.Result.Error -> {
                    _state.value = _state.value.copy(isLoadingMyCharacters = false)
                }
            }
        }
    }

    private fun refreshMyCharactersSilently() {
        viewModelScope.launch {
            when (val res = remoteDataSource.getMyCharacters()) {
                is com.dnd.helper.domain.common.Result.Success -> {
                    val current = _state.value
                    if (current.characterTemplates != res.data.templates ||
                        current.standaloneInstances != res.data.standaloneInstances
                    ) {
                        _state.value = _state.value.copy(
                            characterTemplates = res.data.templates,
                            standaloneInstances = res.data.standaloneInstances
                        )
                    }
                }
                is com.dnd.helper.domain.common.Result.Error -> {}
            }
        }
    }

    private fun refreshPendingAssignmentsSilently() {
        viewModelScope.launch {
            when (val res = remoteDataSource.getPendingAssignments()) {
                is com.dnd.helper.domain.common.Result.Success -> {
                    val current = _state.value.pendingAssignments
                    val newAssignments = res.data
                    if (current.size != newAssignments.size ||
                        current.zip(newAssignments).any { (old, new) ->
                            old.assignmentId != new.assignmentId ||
                                old.status != new.status
                        }
                    ) {
                        _state.value = _state.value.copy(
                            pendingAssignments = newAssignments,
                            assignmentError = null
                        )
                    }
                }
                is com.dnd.helper.domain.common.Result.Error -> {}
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
                    _state.value = _state.value.copy(
                        pendingAssignments = _state.value.pendingAssignments.filter { it.assignmentId != assignmentId }
                    )
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
                storage.saveCharacterId(event.characterId)
                storage.saveTableId(event.sessionId)
                _state.value = _state.value.copy(
                    characterId = event.characterId,
                    tableId = event.sessionId
                )
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
            is StartEvent.JoinCampaign -> joinCampaign(event.characterId, event.gameId)
            is StartEvent.DeleteCharacter -> deleteCharacter(event.characterId)
            StartEvent.DismissJoinError -> _state.value = _state.value.copy(joinError = null)
        }
    }

    private fun deleteCharacter(characterId: String) {
        viewModelScope.launch {
            when (remoteDataSource.deleteMyCharacter(characterId)) {
                is com.dnd.helper.domain.common.Result.Success -> {
                    loadMyCharacters()
                }
                is com.dnd.helper.domain.common.Result.Error -> {}
            }
        }
    }

    private fun joinCampaign(characterId: String, gameId: String) {
        val decodedGameId = IdUtils.decode(gameId)
        viewModelScope.launch {
            _state.value = _state.value.copy(isJoiningCampaign = true, joinError = null)
            when (val res = remoteDataSource.joinCampaign(characterId, decodedGameId)) {
                is com.dnd.helper.domain.common.Result.Success -> {
                    _state.value = _state.value.copy(isJoiningCampaign = false)
                    storage.saveTableId(decodedGameId)
                    loadMyCharacters()
                }
                is com.dnd.helper.domain.common.Result.Error -> {
                    _state.value = _state.value.copy(
                        isJoiningCampaign = false,
                        joinError = "Failed to join: ${res.error}"
                    )
                }
            }
        }
    }
}
