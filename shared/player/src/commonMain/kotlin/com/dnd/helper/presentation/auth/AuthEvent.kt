package com.dnd.helper.presentation.auth

sealed class AuthEvent {
    data class OnUsernameChanged(val username: String) : AuthEvent()
    data class OnPasswordChanged(val password: String) : AuthEvent()
    object ToggleMode : AuthEvent()
    object Submit : AuthEvent()
    object ClearError : AuthEvent()
}
