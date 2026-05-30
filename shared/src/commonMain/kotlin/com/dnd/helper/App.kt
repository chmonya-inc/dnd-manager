package com.dnd.helper

import androidx.compose.runtime.Composable
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.theme.DndHelperTheme
import org.koin.compose.KoinApplication
import org.koin.dsl.module

val appModule = module {
    // TODO: Add repositories, use cases, data sources
    factory { com.dnd.helper.presentation.characterlist.CharacterListViewModel() }
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
