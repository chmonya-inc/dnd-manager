package com.dnd.helper.di

import com.dnd.helper.data.remote.KtorRemoteDataSource
import com.dnd.helper.data.repository.CharacterRepositoryImpl
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.theme.ThemeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val coreModule = module {
    // Client for API calls with special headers
    single {
        val scope = this
        val storage = get<com.dnd.helper.domain.storage.CharacterStorage>()

        HttpClient {
            followRedirects = true
            expectSuccess = false
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                        isLenient = true
                        encodeDefaults = true
                    }
                )
            }
            install(WebSockets)
            install(io.ktor.client.plugins.DefaultRequest) {
                header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                header("ngrok-skip-browser-warning", "true")

                // Manually add the token here to ensure it's always up-to-date and reactive to logout/login
                // The Auth plugin's loadTokens can cache tokens internally, but DefaultRequest is called every time.
                try {
                    val token = storage.getAuthToken()
                    if (token != null) {
                        header(io.ktor.http.HttpHeaders.Authorization, "Bearer $token")
                    }
                } catch (_: Exception) {}
            }
            install(io.ktor.client.plugins.auth.Auth) {
                bearer {
                    // We don't need loadTokens anymore as we set the header in DefaultRequest
                    // But we keep refreshTokens to handle 401s automatically
                    refreshTokens {
                        val authRepo = scope.get<com.dnd.helper.domain.repository.AuthRepository>()
                        val result = authRepo.refresh()
                        if (result.isSuccess) {
                            val newToken = result.getOrNull()!!.accessToken
                            io.ktor.client.plugins.auth.providers.BearerTokens(newToken, newToken)
                        } else {
                            // Refresh failed — clear tokens so user is redirected to login
                            storage.saveAuthToken(null)
                            storage.saveRefreshToken(null)
                            null
                        }
                    }
                }
            }
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[Ktor API] $message")
                    }
                }
            }
        }
    }

    // Clean client for Image Loading (Coil) to avoid CORS Preflight
    single(named("imageClient")) {
        HttpClient {
            followRedirects = true
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        println("[Ktor Image] $message")
                    }
                }
            }
        }
    }

    single { KtorRemoteDataSource(get(), get()) }
    single { com.dnd.helper.data.remote.DndApiDataSource(get(), get()) }
    single { com.dnd.helper.data.remote.AiImageService(get(), get()) }
    single<com.dnd.helper.domain.repository.EditingRepository> {
        com.dnd.helper.data.repository.EditingRepositoryImpl(
            get(),
            get()
        )
    }
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }
    single<com.dnd.helper.domain.repository.AuthRepository> {
        com.dnd.helper.data.repository.AuthRepositoryImpl(
            get(),
            get()
        )
    }
    single { ThemeViewModel(get()) }
}
