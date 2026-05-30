package com.dnd.helper

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dnd.helper.data.remote.GoogleAppsScriptDataSource
import com.dnd.helper.data.repository.CharacterRepositoryImpl
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.presentation.characterdetail.CharacterDetailScreen
import com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.characterlist.CharacterListViewModel
import com.dnd.helper.theme.DndHelperTheme
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

@Serializable
object CharacterList

@Serializable
data class CharacterDetail(val id: String)

val appModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { 
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
                logger = Logger.DEFAULT
            }
        }
    }
    single { GoogleAppsScriptDataSource(get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get()) }
    factory { CharacterListViewModel(get()) }
    factory { (characterId: String) ->
        CharacterDetailViewModel(get(), characterId)
    }
}

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        DndHelperTheme {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = CharacterList
            ) {
                composable<CharacterList> {
                    CharacterListScreen(
                        onCharacterClick = { characterId ->
                            navController.navigate(CharacterDetail(id = characterId))
                        }
                    )
                }
                composable<CharacterDetail> { backStackEntry ->
                    val detailRoute: CharacterDetail = backStackEntry.toRoute()
                    val viewModel: CharacterDetailViewModel = koinViewModel {
                        parametersOf(detailRoute.id)
                    }
                    CharacterDetailScreen(
                        viewModel = viewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
