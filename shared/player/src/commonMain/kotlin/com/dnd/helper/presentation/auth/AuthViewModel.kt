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
            AuthEvent.ToggleRole -> _state.update { it.copy(isMasterRole = !it.isMasterRole) }
            AuthEvent.SetMasterRole -> _state.update { it.copy(isMasterRole = true, requiredRole = "MASTER") }
            is AuthEvent.SetRequiredRole -> _state.update { it.copy(requiredRole = event.role) }
            AuthEvent.Submit -> submit()
            AuthEvent.ClearError -> _state.update { it.copy(error = null, errorRoleMismatch = null) }
        }
    }

    private fun submit() {
        val currentState = _state.value
        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(error = "Username and password cannot be empty") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null, errorRoleMismatch = null) }

        viewModelScope.launch {
            val result = if (currentState.isLoginMode) {
                repository.login(LoginRequest(currentState.username, currentState.password))
            } else {
                val role = if (currentState.isMasterRole) "MASTER" else "PLAYER"
                repository.register(RegisterRequest(currentState.username, currentState.password, role))
            }

            result.onSuccess { response ->
                // Check if role matches if restricted
                if (currentState.requiredRole != null && response.user.role != currentState.requiredRole) {
                    repository.logout() // Clear tokens if role mismatch
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            errorRoleMismatch = if (currentState.requiredRole == "MASTER") 
                                "This account is not a Master account. Please use the Player app." 
                            else 
                                "This account is a Master account. Please use the Desktop app for Master tools."
                        ) 
                    }
                } else {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                }
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
