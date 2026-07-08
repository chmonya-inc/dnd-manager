package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.KtorRemoteDataSource
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssignCharacterState(
    val username: String = "",
    val isAssigning: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AssignCharacterViewModel(
    private val remoteDataSource: KtorRemoteDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(AssignCharacterState())
    val state = _state.asStateFlow()

    fun onUsernameChanged(username: String) {
        _state.value = _state.value.copy(username = username, error = null, success = false)
    }

    fun assignCharacter(characterId: String, sessionId: String) {
        val username = _state.value.username.trim()
        if (username.isEmpty()) {
            _state.value = _state.value.copy(error = "Username cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isAssigning = true, error = null, success = false)
            when (val res = remoteDataSource.assignCharacterByUsername(characterId, sessionId, username)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isAssigning = false,
                        success = true
                    )
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
        viewModelScope.launch {
            _state.value = _state.value.copy(isAssigning = true, error = null, success = false)
            when (val res = remoteDataSource.assignCharacterByUsername(characterId, sessionId, null)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        isAssigning = false,
                        success = true,
                        username = ""
                    )
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
}
