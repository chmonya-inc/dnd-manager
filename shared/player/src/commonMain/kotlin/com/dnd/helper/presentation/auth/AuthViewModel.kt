package com.dnd.helper.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.dto.auth.LoginRequest
import com.dnd.helper.data.remote.dto.auth.RegisterRequest
import com.dnd.helper.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state = _state.asStateFlow()

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.OnUsernameChanged -> _state.update { it.copy(username = event.username) }
            is AuthEvent.OnPasswordChanged -> _state.update { it.copy(password = event.password) }
            AuthEvent.ToggleMode -> _state.update { it.copy(isLoginMode = !it.isLoginMode, error = null) }
            AuthEvent.Submit -> submit()
            AuthEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }

    private fun submit() {
        val currentState = _state.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = "Username and password cannot be empty") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = if (currentState.isLoginMode) {
                repository.login(LoginRequest(currentState.username, currentState.password))
            } else {
                repository.register(RegisterRequest(currentState.username, currentState.password))
            }

            result.onSuccess {
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An unknown error occurred"
                    )
                }
            }
        }
    }
}
