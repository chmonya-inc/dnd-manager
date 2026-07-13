package com.dnd.helper.presentation.charactercreate

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayerCharacterEditScreen(
    characterId: String,
    onBackClick: () -> Unit,
    onCharacterUpdated: (String) -> Unit,
    viewModel: PlayerCharacterCreateViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(characterId) {
        viewModel.onEvent(PlayerCharacterCreateEvent.LoadCharacter(characterId))
    }

    PlayerCharacterForm(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onSaved = onCharacterUpdated,
        snackbarHostState = snackbarHostState
    )
}
