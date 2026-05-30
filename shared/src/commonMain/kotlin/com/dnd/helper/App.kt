package com.dnd.helper

import androidx.compose.runtime.Composable
import com.dnd.helper.data.remote.GoogleAppsScriptDataSource
import com.dnd.helper.data.repository.CharacterRepositoryImpl
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.theme.DndHelperTheme
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.compose.KoinApplication
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { GoogleAppsScriptDataSource(get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get()) }
    factory { com.dnd.helper.presentation.characterlist.CharacterListViewModel(get()) }
}

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        DndHelperTheme {
            CharacterListScreen(
                onCharacterClick = { characterId ->
                    // TODO: Navigate to character detail
                },
            )
        }
    }
}
