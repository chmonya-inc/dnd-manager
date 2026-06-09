package com.dnd.helper

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dnd.helper.presentation.charactercreate.CharacterCreateScreen
import com.dnd.helper.presentation.charactercreate.CharacterCreateViewModel
import com.dnd.helper.presentation.characterdetail.CharacterDetailScreen
import com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.desktop.*
import com.dnd.helper.presentation.start.StartScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

@Serializable
object CharacterCreate

@Serializable
object MainDesktop

@Serializable
object Library

@Serializable
object Creator

@Serializable
object Presenter

val desktopModule = module {
    factory { LibraryViewModel(get(), get()) }
    factory { RulesLibraryViewModel(get()) }
    factory { CharacterCreateViewModel(get(), get(), get()) }
    factory { com.dnd.helper.presentation.monstercreate.MonsterCreateViewModel(get(), get(), get()) }
    factory { com.dnd.helper.presentation.itemcreate.ItemCreateViewModel(get(), get(), get()) }
    factory { LogViewModel(get()) }
    factory { MusicViewModel(get(), get()) }
    factory { SessionsViewModel(get(), get()) }
    factory { SettingsViewModel(get()) }
    single { PresentationViewModel(get()) }
}

@Composable
fun DesktopApp(koinConfiguration: KoinAppDeclaration = {}) {
    CoreApp(
        koinConfiguration = koinConfiguration,
        appModules = listOf(playerModule, desktopModule)
    ) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = MainDesktop
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
