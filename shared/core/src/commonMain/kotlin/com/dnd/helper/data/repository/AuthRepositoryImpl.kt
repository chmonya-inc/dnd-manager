package com.dnd.helper.data.repository

import com.dnd.helper.data.remote.dto.auth.AuthResponse
import com.dnd.helper.data.remote.dto.auth.LoginRequest
import com.dnd.helper.data.remote.dto.auth.RefreshRequest
import com.dnd.helper.data.remote.dto.auth.RegisterRequest
import com.dnd.helper.domain.repository.AuthRepository
import com.dnd.helper.domain.storage.CharacterStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthRepositoryImpl(
    private val client: HttpClient,
    private val storage: CharacterStorage
) : AuthRepository {

    private fun baseUrl(address: String? = storage.getServerAddress()): String {
        if (address != null && address.isNotBlank()) {
            var formatted = address.trim()
            if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                formatted = "http://$formatted"
            }
            return formatted.removeSuffix("/exec").removeSuffix("/")
        }
        return com.dnd.helper.data.config.GoogleAppsScriptConfig.WEB_APP_URL.removeSuffix("/exec").removeSuffix("/")
    }

    override suspend fun login(request: LoginRequest): Result<AuthResponse> =
        authRequest("${baseUrl()}/auth/login", request, "Login")

    override suspend fun register(request: RegisterRequest): Result<AuthResponse> =
        authRequest("${baseUrl()}/auth/register", request, "Registration")

    override suspend fun refresh(): Result<AuthResponse> {
        val refreshToken = storage.getRefreshToken()
            ?: return Result.failure(Exception("No refresh token"))

        return authRequest(
            "${baseUrl()}/auth/refresh",
            RefreshRequest(refreshToken),
            "Token refresh"
        )
    }

    override fun getAuthToken(): String? = storage.getAuthToken()

    override fun getRefreshToken(): String? = storage.getRefreshToken()

    override fun saveAuthToken(token: String?) = storage.saveAuthToken(token)

    override fun getUserId(): String? = storage.getUserId()

    override fun getUserRole(): String? = storage.getUserRole()

    override suspend fun logout() {
        val refreshToken = storage.getRefreshToken()
        // Best-effort: tell the server to revoke the refresh token.
        // We clear local storage regardless of network outcome.
        if (!refreshToken.isNullOrBlank()) {
            try {
                client.post("${baseUrl()}/auth/logout") {
                    contentType(ContentType.Application.Json)
                    setBody(RefreshRequest(refreshToken))
                }
            } catch (_: Exception) {
                // Network failure — token will naturally expire server-side;
                // local tokens are still cleared below.
            }
        }
        storage.saveAuthToken(null)
        storage.saveRefreshToken(null)
        storage.saveUserId(null)
        storage.saveUserRole(null)
        storage.saveCharacterId("")
        storage.saveTableId("")
    }

    private suspend inline fun <reified T> authRequest(
        url: String,
        body: T,
        operationName: String
    ): Result<AuthResponse> =
        try {
            val response = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
            if (response.status.isSuccess()) {
                val authResponse = response.body<AuthResponse>()
                storage.saveAuthToken(authResponse.accessToken)
                storage.saveRefreshToken(authResponse.refreshToken)
                storage.saveUserId(authResponse.user.id)
                storage.saveUserRole(authResponse.user.role)
                Result.success(authResponse)
            } else {
                val errorBody = try {
                    response.body<String>()
                } catch (_: Exception) {
                    response.status.description
                }
                Result.failure(Exception("$operationName failed: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
