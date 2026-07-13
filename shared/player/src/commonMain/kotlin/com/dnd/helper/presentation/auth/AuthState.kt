package com.dnd.helper.presentation.auth

data class AuthState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginMode: Boolean = true,
    val isRecoverMode: Boolean = false,
    val newPassword: String = "",
    val registeredRecoverCode: String? = null,
    val isSuccess: Boolean = false,
    val isMasterRole: Boolean = false, // Only meaningful in register mode
    val requiredRole: String? = null,
    val errorRoleMismatch: String? = null
)
