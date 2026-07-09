package com.dnd.helper.presentation.start

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.KtorRemoteDataSource
import com.dnd.helper.domain.common.IdUtils
import com.dnd.helper.domain.repository.AuthRepository
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StartViewModel(
    private val storage: CharacterStorage,
    private val remoteDataSource: KtorRemoteDataSource,
    private val authRepository: AuthRepository,
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
            StartEvent.RefreshMyCharacters -> loadMyCharacters()
            StartEvent.Logout -> {
                viewModelScope.launch {
                    authRepository.logout()
                }
            }
        }
    }
}
