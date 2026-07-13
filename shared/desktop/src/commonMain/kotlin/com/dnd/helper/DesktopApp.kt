package com.dnd.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dnd.helper.presentation.charactercreate.CharacterCreateScreen
import com.dnd.helper.presentation.charactercreate.CharacterCreateViewModel
import com.dnd.helper.presentation.characterdetail.CharacterDetailScreen
import com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.desktop.AssignCharacterViewModel
import com.dnd.helper.presentation.desktop.CampaignPickerScreen
import com.dnd.helper.presentation.desktop.LibraryViewModel
import com.dnd.helper.presentation.desktop.LogViewModel
import com.dnd.helper.presentation.desktop.MainDesktopScreen
import com.dnd.helper.presentation.desktop.MusicViewModel
import com.dnd.helper.presentation.desktop.PresentationViewModel
import com.dnd.helper.presentation.desktop.RulesLibraryViewModel
import com.dnd.helper.presentation.desktop.SessionsViewModel
import com.dnd.helper.presentation.desktop.SettingsViewModel
import com.dnd.helper.presentation.start.StartScreen
import kotlinx.coroutines.launch
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
object CampaignPicker

@Serializable
object Library

@Serializable
object Creator

@Serializable
object Presenter

val desktopModule = module {
    factory { LibraryViewModel(get(), get()) }
    factory { RulesLibraryViewModel(get()) }
    factory { CharacterCreateViewModel(get(), get(), get(), get(), get()) }
    factory { com.dnd.helper.presentation.monstercreate.MonsterCreateViewModel(get(), get(), get()) }
    factory { com.dnd.helper.presentation.itemcreate.ItemCreateViewModel(get(), get(), get()) }
    factory { LogViewModel(get()) }
    factory { MusicViewModel(get(), get()) }
    factory { SessionsViewModel(get(), get(), get()) }
    factory { SettingsViewModel(get(), get()) }
    factory { AssignCharacterViewModel(get(), get()) }
    single { PresentationViewModel(get()) }
}

@Composable
fun DesktopApp(koinConfiguration: KoinAppDeclaration = {}) {
    CoreApp(
        koinConfiguration = koinConfiguration,
        appModules = listOf(playerModule, desktopModule)
    ) {
        val navController = rememberNavController()
        val authRepository = org.koin.compose.koinInject<com.dnd.helper.domain.repository.AuthRepository>()
        val storage = org.koin.compose.koinInject<com.dnd.helper.domain.storage.CharacterStorage>()

        val startDest: Any = if (authRepository.getRefreshToken() != null) {
            if (storage.getTableId().isNullOrBlank()) CampaignPicker else MainDesktop
        } else {
            AuthRoute
        }

        var showSettings by remember { mutableStateOf(false) }

        if (showSettings) {
            com.dnd.helper.presentation.desktop.SettingsDialog(
                onDismiss = { showSettings = false }
            )
        }

        NavHost(
            navController = navController,
            startDestination = startDest
        ) {
            composable<AuthRoute> {
                com.dnd.helper.presentation.auth.AuthScreen(
                    forceMasterRole = true,
                    onAuthSuccess = {
                        navController.navigate(CampaignPicker) {
                            popUpTo(AuthRoute) { inclusive = true }
                        }
                    },
                    onSettingsClick = { showSettings = true }
                )
            }
            composable<CampaignPicker> {
                val coroutineScope = rememberCoroutineScope()
                CampaignPickerScreen(
                    onCampaignSelected = {
                        navController.navigate(MainDesktop) {
                            popUpTo(CampaignPicker) { inclusive = true }
                        }
                    },
                    onLogout = {
                        coroutineScope.launch {
                            authRepository.logout()
                            navController.navigate(AuthRoute) {
                                popUpTo(CampaignPicker) { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable<MainDesktop> {
                MainDesktopScreen(
                    onLogout = {
                        navController.navigate(AuthRoute) {
                            popUpTo(MainDesktop) { inclusive = true }
                        }
                    },
                    onSwitchCampaign = {
                        navController.navigate(CampaignPicker) {
                            popUpTo(MainDesktop) { inclusive = true }
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
                        navController.navigate(CharacterCreate)
                    }
                )
            }
            composable<CharacterCreate> {
                CharacterCreateScreen(
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
