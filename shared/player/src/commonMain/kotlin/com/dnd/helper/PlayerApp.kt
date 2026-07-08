package com.dnd.helper

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dnd.helper.presentation.characterdetail.CharacterDetailScreen
import com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.characterlist.CharacterListViewModel
import com.dnd.helper.presentation.start.StartScreen
import com.dnd.helper.presentation.start.StartViewModel
import kotlinx.serialization.Serializable
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

@Serializable
object AuthRoute

val playerModule = module {
    factory { com.dnd.helper.presentation.auth.AuthViewModel(get()) }
    factory { StartViewModel(get(), get(), get()) }
    factory { CharacterListViewModel(get(), get(), get()) }
    factory { (characterId: String) ->
        CharacterDetailViewModel(get(), get(), characterId)
    }
}

@Composable
fun PlayerApp(koinConfiguration: KoinAppDeclaration = {}) {
    CoreApp(
        koinConfiguration = koinConfiguration,
        appModules = listOf(playerModule)
    ) {
        val navController = rememberNavController()
        val authRepository = org.koin.compose.koinInject<com.dnd.helper.domain.repository.AuthRepository>()
        val startDest: Any = if (authRepository.getRefreshToken() != null) Start else AuthRoute

        NavHost(
            navController = navController,
            startDestination = startDest
        ) {
            composable<AuthRoute> {
                com.dnd.helper.presentation.auth.AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Start) {
                            popUpTo(AuthRoute) { inclusive = true }
                        }
                    }
                )
            }
            composable<Start> {
                StartScreen(
                    onLoadCharacter = { characterId ->
                        navController.navigate(CharacterDetail(id = characterId))
                    },
                    onLogout = {
                        navController.navigate(AuthRoute) {
                            popUpTo(Start) { inclusive = true }
                        }
                    }
                )
            }
            composable<CharacterList> {
                CharacterListScreen(
                    onCharacterClick = { characterId ->
                        navController.navigate(CharacterDetail(id = characterId))
                    },
                    onCreateCharacter = {
                        // For PlayerApp, creating characters might not be supported or we could navigate to a web form
                        // We will just leave it empty or print a log since player app is supposed to be view only for characters,
                        // unless we keep character create in player. The plan says CharacterCreate is in desktop.
                        // Let's just do nothing or maybe we need to bring it?
                        // "For PlayerApp, creating characters might not be supported"
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
