package com.dnd.helper.domain.repository

import com.dnd.helper.data.remote.dto.auth.AuthResponse
import com.dnd.helper.data.remote.dto.auth.LoginRequest
import com.dnd.helper.data.remote.dto.auth.RegisterRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponse>
    suspend fun register(request: RegisterRequest): Result<AuthResponse>
    suspend fun recover(request: com.dnd.helper.data.remote.dto.auth.PasswordRecoveryRequest): Result<AuthResponse>
    suspend fun refresh(): Result<AuthResponse>
    fun getAuthToken(): String?
    fun getRefreshToken(): String?
    fun saveAuthToken(token: String?)
    fun getUserId(): String?
    fun getUserRole(): String?
    /** Calls POST /auth/logout on the server to revoke the refresh token, then clears local storage. */
    suspend fun logout()
}
