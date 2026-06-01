package com.dnd.helper

import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.dnd.helper.data.remote.GoogleAppsScriptDataSource
import com.dnd.helper.data.repository.CharacterRepositoryImpl
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.di.isDesktop
import com.dnd.helper.di.platformModule
import com.dnd.helper.presentation.charactercreate.CharacterCreateScreen
import com.dnd.helper.presentation.charactercreate.CharacterCreateViewModel
import com.dnd.helper.presentation.characterdetail.CharacterDetailScreen
import com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.characterlist.CharacterListViewModel
import com.dnd.helper.presentation.desktop.*
import com.dnd.helper.presentation.start.StartScreen
import com.dnd.helper.presentation.start.StartViewModel
import com.dnd.helper.theme.DndHelperTheme
import com.dnd.helper.theme.ThemeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import kotlinx.serialization.Serializable
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

@Serializable
object CharacterList

@Serializable
object Start

@Serializable
object CharacterCreate

@Serializable
data class CharacterDetail(val id: String)

@Serializable
object MainDesktop

@Serializable
object Library

@Serializable
object Creator

@Serializable
object Presenter

val appModule = module {
    // No ContentNegotiation — GoogleAppsScriptDataSource reads raw response.
    // We add a User-Agent to avoid being blocked by Google Drive.
    single {
        HttpClient {
            followRedirects = true
            expectSuccess = false // We handle status checks manually in DataSource
            install(io.ktor.client.plugins.DefaultRequest) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            }
            // Add logging to help diagnose server issues
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

    single { GoogleAppsScriptDataSource(get(), get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }
    factory { CharacterListViewModel(get(), get()) }
    factory { StartViewModel(get()) }
    factory { CharacterCreateViewModel(get()) }
    factory { (characterId: String) ->
        CharacterDetailViewModel(get(), characterId)
    }
    factory { LibraryViewModel(get()) }
    factory { LogViewModel(get()) }
    factory { MusicViewModel(get(), get()) }
    single { ThemeViewModel(get()) }
    single { com.dnd.helper.presentation.desktop.PresentationViewModel(get()) }
}

@Composable
fun App(koinConfiguration: KoinAppDeclaration = {}) {
    KoinApplication(application = {
        koinConfiguration()
        modules(appModule, platformModule)
    }) {
        // Initialize Coil's singleton ImageLoader.
        // We pass the Koin-managed HttpClient to KtorNetworkFetcherFactory.
        // The client has no ContentNegotiation, so image requests are clean.
        val koin = getKoin()
        val httpClient = remember { koin.get<HttpClient>() }
        remember(httpClient) {
            SingletonImageLoader.setSafe { context ->
                ImageLoader.Builder(context)
                    .components {
                        // Use the shared HttpClient for all network requests.
                        // KtorNetworkFetcher handles images correctly even for JPGs,
                        // provided the server returns appropriate headers.
                        add(KtorNetworkFetcherFactory(httpClient))
                    }
                    .crossfade(true)
                    .logger(DebugLogger()) // Logs detailed info about why JPGs might fail
                    .build()
            }
        }

        val themeViewModel: ThemeViewModel = koinViewModel()
        val currentTheme by themeViewModel.currentTheme.collectAsState()

        DndHelperTheme(theme = currentTheme) {
            val navController = rememberNavController()
            val startDestination = if (isDesktop) MainDesktop else Start

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
                composable<MainDesktop> {
                    MainDesktopScreen()
                }
                composable<Start> {
                    StartScreen(
                        onLoadCharacter = { characterId ->
                            navController.navigate(CharacterDetail(id = characterId))
                        }
                    )
                }
                composable<CharacterList> {
                    CharacterListScreen(
                        onCharacterClick = { characterId ->
                            navController.navigate(CharacterDetail(id = characterId))
                        },
                        onCreateCharacter = {
                            navController.navigate(CharacterCreate)
                        }
                    )
                }
                composable<CharacterCreate> {
                    CharacterCreateScreen(
                        onBackClick = { navController.popBackStack() },
                        onCharacterCreated = {
                            navController.popBackStack()
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
