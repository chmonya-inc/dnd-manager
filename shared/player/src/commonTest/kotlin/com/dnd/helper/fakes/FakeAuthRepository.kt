package com.dnd.helper.fakes

import com.dnd.helper.data.remote.dto.auth.AuthResponse
import com.dnd.helper.data.remote.dto.auth.LoginRequest
import com.dnd.helper.data.remote.dto.auth.PasswordRecoveryRequest
import com.dnd.helper.data.remote.dto.auth.RegisterRequest
import com.dnd.helper.domain.repository.AuthRepository

class FakeAuthRepository : AuthRepository {
    var storedUserRole: String? = "PLAYER"
    var logoutCalled = false
    var loginCalled = false
    var registerCalled = false
    var recoverCalled = false

    // Test control properties
    var loginShouldSucceed: Boolean = false
    var registerShouldSucceed: Boolean = false
    var recoverShouldSucceed: Boolean = false
    var refreshShouldSucceed: Boolean = false
    var loginError: String? = null
    var registerError: String? = null
    var recoverError: String? = null
    var refreshError: String? = null
    var userRoleInResponse: String = "PLAYER"
    var recoverCode: String? = null
    var loginDelay: Long = 50
    var registerDelay: Long = 50
    var recoverDelay: Long = 50
    var refreshDelay: Long = 50

    // Track call parameters
    val loginCalls = mutableListOf<LoginRequest>()
    val registerCalls = mutableListOf<RegisterRequest>()
    val recoverCalls = mutableListOf<PasswordRecoveryRequest>()
    var lastRegisteredRole: String? = null

    override suspend fun login(request: LoginRequest): Result<AuthResponse> {
        loginCalled = true
        loginCalls.add(request)
        kotlinx.coroutines.delay(loginDelay)

        return if (loginShouldSucceed) {
            Result.success(AuthResponse(
                accessToken = "test-access-token",
                refreshToken = "test-refresh-token",
                user = com.dnd.helper.data.remote.dto.auth.UserDto(
                    id = "user-123",
                    username = request.username,
                    role = userRoleInResponse
                )
            ))
        } else {
            Result.failure(Exception(loginError ?: "Login failed"))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        registerCalled = true
        registerCalls.add(request)
        lastRegisteredRole = request.role
        kotlinx.coroutines.delay(registerDelay)

        return if (registerShouldSucceed) {
            Result.success(AuthResponse(
                accessToken = "test-access-token",
                refreshToken = "test-refresh-token",
                user = com.dnd.helper.data.remote.dto.auth.UserDto(
                    id = "user-123",
                    username = request.username,
                    role = request.role
                ),
                recoverCode = recoverCode
            ))
        } else {
            Result.failure(Exception(registerError ?: "Registration failed"))
        }
    }

    override suspend fun recover(request: PasswordRecoveryRequest): Result<AuthResponse> {
        recoverCalled = true
        recoverCalls.add(request)
        kotlinx.coroutines.delay(recoverDelay)

        return if (recoverShouldSucceed) {
            Result.success(AuthResponse(
                accessToken = "test-access-token",
                refreshToken = "test-refresh-token",
                user = com.dnd.helper.data.remote.dto.auth.UserDto(
                    id = "user-123",
                    username = request.username,
                    role = userRoleInResponse
                )
            ))
        } else {
            Result.failure(Exception(recoverError ?: "Password recovery failed"))
        }
    }

    override suspend fun refresh(): Result<AuthResponse> {
        kotlinx.coroutines.delay(refreshDelay)

        return if (refreshShouldSucceed) {
            Result.success(AuthResponse(
                accessToken = "test-access-token",
                refreshToken = "test-refresh-token",
                user = com.dnd.helper.data.remote.dto.auth.UserDto(
                    id = "user-123",
                    username = "testuser",
                    role = userRoleInResponse
                )
            ))
        } else {
            Result.failure(Exception(refreshError ?: "Token refresh failed"))
        }
    }

    override fun getAuthToken(): String? = "fake-token"
    override fun getRefreshToken(): String? = "fake-refresh"
    override fun saveAuthToken(token: String?) {}
    override fun getUserRole(): String? = storedUserRole
    override fun getUserId(): String? = "fake-user-id"

    override suspend fun logout() {
        logoutCalled = true
    }

    fun reset() {
        storedUserRole = "PLAYER"
        logoutCalled = false
        loginCalled = false
        registerCalled = false
        recoverCalled = false
        loginShouldSucceed = false
        registerShouldSucceed = false
        recoverShouldSucceed = false
        refreshShouldSucceed = false
        loginError = null
        registerError = null
        recoverError = null
        refreshError = null
        userRoleInResponse = "PLAYER"
        recoverCode = null
        loginDelay = 50
        registerDelay = 50
        recoverDelay = 50
        refreshDelay = 50
        loginCalls.clear()
        registerCalls.clear()
        recoverCalls.clear()
        lastRegisteredRole = null
    }
}
