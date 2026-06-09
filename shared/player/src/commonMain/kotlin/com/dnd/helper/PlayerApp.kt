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

val playerModule = module {
    factory { StartViewModel(get()) }
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

        NavHost(
            navController = navController,
            startDestination = Start
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
