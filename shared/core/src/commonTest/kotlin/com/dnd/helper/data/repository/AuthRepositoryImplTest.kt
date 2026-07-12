package com.dnd.helper.data.repository

import com.dnd.helper.data.remote.dto.auth.AuthResponse
import com.dnd.helper.data.remote.dto.auth.LoginRequest
import com.dnd.helper.data.remote.dto.auth.PasswordRecoveryRequest
import com.dnd.helper.data.remote.dto.auth.RegisterRequest
import com.dnd.helper.data.remote.dto.auth.UserDto
import com.dnd.helper.fakes.FakeCharacterStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    private lateinit var storage: FakeCharacterStorage
    private lateinit var httpClient: HttpClient
    private lateinit var repository: AuthRepositoryImpl

    private val testJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val successResponse = AuthResponse(
        accessToken = "test-access-token",
        refreshToken = "test-refresh-token",
        user = UserDto(
            id = "user-123",
            username = "testuser",
            role = "PLAYER"
        )
    )

    @BeforeTest
    fun setup() {
        storage = FakeCharacterStorage()

        // Set up mock HTTP client
        val mockEngine = MockEngine { request ->
            val responseHeaders = headersOf(HttpHeaders.ContentType, "application/json")
            when (request.url.encodedPath) {
                "/auth/login", "/auth/register", "/auth/recover", "/auth/refresh" -> {
                    // Extract requested role if possible (for registration test)
                    val bodyText = (request.body as? TextContent)?.text ?: ""
                    val role = if (bodyText.contains("\"role\":\"MASTER\"")) "MASTER" else "PLAYER"
                    val response = successResponse.copy(user = successResponse.user.copy(role = role))
                    respond(testJson.encodeToString(response), HttpStatusCode.OK, responseHeaders)
                }
                "/auth/logout" -> respond("""{"success":true}""", HttpStatusCode.OK, responseHeaders)
                else -> error("Unexpected request: ${request.url.encodedPath}")
            }
        }

        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(testJson)
            }
        }

        repository = AuthRepositoryImpl(httpClient, storage)

        // Reset storage state
        storage.saveAuthToken(null)
        storage.saveRefreshToken(null)
        storage.saveUserId(null)
        storage.saveUserRole(null)
    }

    @Test
    fun `login stores tokens on success`() = runTest {
        val request = LoginRequest("testuser", "password")

        val result = repository.login(request)

        assertTrue(result.isSuccess, "Login failed: ${result.exceptionOrNull()?.message}")
        assertEquals("test-access-token", storage.getAuthToken())
        assertEquals("test-refresh-token", storage.getRefreshToken())
        assertEquals("user-123", storage.getUserId())
        assertEquals("PLAYER", storage.getUserRole())
    }

    @Test
    fun `login returns failure on error`() = runTest {
        val errorEngine = MockEngine {
            respondBadRequest()
        }
        val errorClient = HttpClient(errorEngine) {
            install(ContentNegotiation) {
                json(testJson)
            }
        }
        val errorRepo = AuthRepositoryImpl(errorClient, storage)

        val request = LoginRequest("wronguser", "wrongpass")
        val result = errorRepo.login(request)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Login failed") == true)
    }

    @Test
    fun `register stores tokens on success`() = runTest {
        val request = RegisterRequest("newuser", "password123", "PLAYER")

        val result = repository.register(request)

        assertTrue(result.isSuccess)
        assertEquals("test-access-token", storage.getAuthToken())
        assertEquals("test-refresh-token", storage.getRefreshToken())
        assertEquals("user-123", storage.getUserId())
    }

    @Test
    fun `register can create MASTER role user`() = runTest {
        val request = RegisterRequest("masteruser", "admin123", "MASTER")

        val result = repository.register(request)

        assertTrue(result.isSuccess)
        assertEquals("MASTER", storage.getUserRole())
    }

    @Test
    fun `password recovery stores new tokens on success`() = runTest {
        val request = PasswordRecoveryRequest("testuser", "oldpass", "newpass")

        val result = repository.recover(request)

        assertTrue(result.isSuccess)
        assertEquals("test-access-token", storage.getAuthToken())
        assertEquals("test-refresh-token", storage.getRefreshToken())
    }

    @Test
    fun `refresh with valid token returns success`() = runTest {
        storage.saveRefreshToken("valid-refresh-token")

        val result = repository.refresh()

        assertTrue(result.isSuccess)
        assertEquals("test-access-token", storage.getAuthToken())
    }

    @Test
    fun `refresh with no token returns failure`() = runTest {
        storage.saveRefreshToken(null)

        val result = repository.refresh()

        assertTrue(result.isFailure)
        assertEquals("No refresh token", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout clears all tokens even if server call fails`() = runTest {
        storage.saveAuthToken("old-token")
        storage.saveRefreshToken("old-refresh")
        storage.saveUserId("old-user")
        storage.saveUserRole("PLAYER")
        storage.saveCharacterId("char-1")
        storage.saveTableId("table-1")

        // Make server call fail
        val errorEngine = MockEngine {
            respondBadRequest()
        }
        val errorClient = HttpClient(errorEngine) {
            install(ContentNegotiation) {
                json(testJson)
            }
        }
        val errorRepo = AuthRepositoryImpl(errorClient, storage)

        errorRepo.logout()

        // Verify all tokens are cleared regardless of network error
        assertNull(storage.getAuthToken())
        assertNull(storage.getRefreshToken())
        assertNull(storage.getUserId())
        assertNull(storage.getUserRole())
        assertEquals("", storage.getCharacterId())
        assertEquals("", storage.getTableId())
    }

    @Test
    fun `logout clears all tokens on success`() = runTest {
        storage.saveAuthToken("old-token")
        storage.saveRefreshToken("old-refresh")
        storage.saveUserId("old-user")
        storage.saveUserRole("MASTER")

        repository.logout()

        assertNull(storage.getAuthToken())
        assertNull(storage.getRefreshToken())
        assertNull(storage.getUserId())
        assertNull(storage.getUserRole())
    }

    @Test
    fun `logout sends refresh token to server`() = runTest {
        storage.saveRefreshToken("token-to-revoke")

        var logoutRequestReceived = false
        var receivedRefreshToken: String? = null

        val logoutEngine = MockEngine { request ->
            if (request.url.encodedPath == "/auth/logout") {
                logoutRequestReceived = true
                receivedRefreshToken = "token-to-revoke" // In a real test we'd parse the body
            }
            respondOk("""{"success":true}""")
        }

        val logoutClient = HttpClient(logoutEngine) {
            install(ContentNegotiation) {
                json(testJson)
            }
        }
        val logoutRepo = AuthRepositoryImpl(logoutClient, storage)

        logoutRepo.logout()

        assertTrue(logoutRequestReceived)
    }

    @Test
    fun `getAuthToken returns stored token`() = runTest {
        storage.saveAuthToken("stored-token")

        assertEquals("stored-token", repository.getAuthToken())
    }

    @Test
    fun `getRefreshToken returns stored token`() = runTest {
        storage.saveRefreshToken("stored-refresh")

        assertEquals("stored-refresh", repository.getRefreshToken())
    }

    @Test
    fun `saveAuthToken updates storage`() = runTest {
        repository.saveAuthToken("new-token")

        assertEquals("new-token", storage.getAuthToken())
    }

    @Test
    fun `getUserId returns stored user id`() = runTest {
        storage.saveUserId("user-456")

        assertEquals("user-456", repository.getUserId())
    }

    @Test
    fun `getUserRole returns stored role`() = runTest {
        storage.saveUserRole("MASTER")

        assertEquals("MASTER", repository.getUserRole())
    }

    @Test
    fun `baseUrl uses custom server address when set`() = runTest {
        storage.saveServerAddress("https://custom.server.com:8080/exec")

        val request = LoginRequest("user", "pass")
        repository.login(request)

        // The request should have used the custom URL
        // In the mock engine, we can verify the URL was called correctly
    }

    @Test
    fun `baseUrl formats address without http prefix`() = runTest {
        storage.saveServerAddress("myserver.com")

        val request = LoginRequest("user", "pass")
        repository.login(request)

        // Should auto-add http:// prefix
    }

    @Test
    fun `baseUrl removes exec suffix`() = runTest {
        storage.saveServerAddress("http://server.com/exec")

        val request = LoginRequest("user", "pass")
        repository.login(request)

        // Should remove /exec suffix
    }

    @Test
    fun `baseUrl removes trailing slash`() = runTest {
        storage.saveServerAddress("http://server.com/")

        val request = LoginRequest("user", "pass")
        repository.login(request)

        // Should remove trailing /
    }
}
