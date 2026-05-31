package com.dnd.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import com.dnd.helper.data.remote.GoogleAppsScriptDataSource
import com.dnd.helper.data.repository.CharacterRepositoryImpl
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.di.isDesktop
import com.dnd.helper.di.platformModule
import com.dnd.helper.presentation.characterdetail.CharacterDetailScreen
import com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.characterlist.CharacterListViewModel
import com.dnd.helper.presentation.start.StartScreen
import com.dnd.helper.presentation.start.StartViewModel
import com.dnd.helper.theme.DndHelperTheme
import io.ktor.client.HttpClient
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
data class CharacterDetail(val id: String)

val appModule = module {
    // Plain HttpClient — no ContentNegotiation.
    // GoogleAppsScriptDataSource reads raw response body as String and parses
    // JSON manually with kotlinx.serialization, so ContentNegotiation is unnecessary.
    // Keeping it clean also means we can safely re-use this client for Coil image
    // fetching without JSON Accept headers breaking image requests.
    single { HttpClient() }

    single { GoogleAppsScriptDataSource(get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get()) }
    factory { CharacterListViewModel(get(), get()) }
    factory { StartViewModel(get()) }
    factory { (characterId: String) ->
        CharacterDetailViewModel(get(), characterId)
    }
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
                        add(KtorNetworkFetcherFactory ({ httpClient }))
                    }
                    .crossfade(true)
                    .build()
            }
        }

        DndHelperTheme {
            val navController = rememberNavController()
            val startDestination = if (isDesktop) CharacterList else Start

            NavHost(
                navController = navController,
                startDestination = startDestination
            ) {
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
