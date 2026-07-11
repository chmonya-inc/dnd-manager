package com.dnd.helper

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dnd.helper.presentation.charactercreate.PlayerCharacterCreateScreen
import com.dnd.helper.presentation.charactercreate.PlayerCharacterCreateViewModel
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

@Serializable
object PlayerCharacterCreate

@Serializable
data class PlayerCharacterEdit(val id: String)

val playerModule = module {
    factory { com.dnd.helper.presentation.auth.AuthViewModel(get()) }
    factory { StartViewModel(get(), get(), get(), get()) }
    factory { CharacterListViewModel(get(), get(), get()) }
    factory { (characterId: String) ->
        CharacterDetailViewModel(get(), get(), characterId)
    }
    factory { PlayerCharacterCreateViewModel(get(), get(), get(), get()) }
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
                    },
                    onCreateCharacter = {
                        navController.navigate(PlayerCharacterCreate)
                    },
                    onEditCharacter = { characterId ->
                        navController.navigate(PlayerCharacterEdit(id = characterId))
                    }
                )
            }
            composable<PlayerCharacterCreate> {
                PlayerCharacterCreateScreen(
                    onBackClick = { navController.popBackStack() },
                    onCharacterCreated = { _ ->
                        // Go back to Start; StartScreen will reload my-characters on resume
                        navController.popBackStack()
                    }
                )
            }
            composable<PlayerCharacterEdit> { backStackEntry ->
                val editRoute: PlayerCharacterEdit = backStackEntry.toRoute()
                com.dnd.helper.presentation.charactercreate.PlayerCharacterEditScreen(
                    characterId = editRoute.id,
                    onBackClick = { navController.popBackStack() },
                    onCharacterUpdated = {
                        navController.popBackStack()
                    }
                )
            }
            composable<CharacterList> {
                CharacterListScreen(
                    onCharacterClick = { characterId ->
                        navController.navigate(CharacterDetail(id = characterId))
                    },
                    onCreateCharacter = {
                        navController.navigate(PlayerCharacterCreate)
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
