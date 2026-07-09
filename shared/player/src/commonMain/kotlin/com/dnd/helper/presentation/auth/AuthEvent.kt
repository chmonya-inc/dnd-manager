package com.dnd.helper.presentation.auth

sealed class AuthEvent {
    data class OnUsernameChanged(val username: String) : AuthEvent()
    data class OnPasswordChanged(val password: String) : AuthEvent()
    object ToggleMode : AuthEvent()
    object ToggleRole : AuthEvent()
    object SetMasterRole : AuthEvent()
    data class SetRequiredRole(val role: String) : AuthEvent()
    object Submit : AuthEvent()
    object ClearError : AuthEvent()
    object PasteUsername : AuthEvent()
    object PastePassword : AuthEvent()
}
