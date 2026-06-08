package com.dnd.helper.di

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.dnd.helper.data.remote.KtorRemoteDataSource
import com.dnd.helper.data.repository.CharacterRepositoryImpl
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.theme.ThemeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val coreModule = module {
    single {
        HttpClient {
            followRedirects = true
            expectSuccess = false 
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            install(WebSockets)
            install(io.ktor.client.plugins.DefaultRequest) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                header("ngrok-skip-browser-warning", "true")
            }
            install(io.ktor.client.plugins.logging.Logging) {
                level = io.ktor.client.plugins.logging.LogLevel.INFO
                logger = object : io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        println("[Ktor] $message")
                    }
                }
            }
        }
    }

    single { KtorRemoteDataSource(get(), get()) }
    single { com.dnd.helper.data.remote.DndApiDataSource(get(), get()) }
    single { com.dnd.helper.data.remote.AiImageService(get(), get()) }
    single<com.dnd.helper.domain.repository.EditingRepository> { com.dnd.helper.data.repository.EditingRepositoryImpl(get(), get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }
    single { ThemeViewModel(get()) }
}
