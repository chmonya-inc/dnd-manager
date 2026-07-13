package com.dnd.helper.fakes

import com.dnd.helper.data.remote.dto.auth.AuthResponse
import com.dnd.helper.data.remote.dto.auth.LoginRequest
import com.dnd.helper.data.remote.dto.auth.PasswordRecoveryRequest
import com.dnd.helper.data.remote.dto.auth.RegisterRequest
import com.dnd.helper.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var logoutCalled = false
    var storedAuthToken: String? = null
    var storedRefreshToken: String? = null
    var storedUserId: String? = null
    var storedUserRole: String? = "PLAYER"

    var loginResult: Result<AuthResponse> = Result.failure(Exception("not implemented"))
    var registerResult: Result<AuthResponse> = Result.failure(Exception("not implemented"))
    var refreshResult: Result<AuthResponse> = Result.failure(Exception("not implemented"))
    var recoverResult: Result<AuthResponse> = Result.failure(Exception("not implemented"))

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return loginResult
    }
    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return registerResult
    }
    override suspend fun recover(request: PasswordRecoveryRequest): Result<AuthResponse> {
        return recoverResult
    }
    override suspend fun refresh(): Result<AuthResponse> {
        return refreshResult
    }

    override fun getAuthToken(): String? = storedAuthToken
    override fun getRefreshToken(): String? = storedRefreshToken
    override fun saveAuthToken(token: String?) {
        storedAuthToken = token
    }
    override fun getUserId(): String? = storedUserId
    override fun getUserRole(): String? = storedUserRole

    override suspend fun logout() {
        logoutCalled = true
    }
}
