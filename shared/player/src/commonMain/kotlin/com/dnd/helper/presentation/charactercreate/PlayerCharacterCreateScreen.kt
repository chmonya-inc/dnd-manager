package com.dnd.helper.presentation.charactercreate

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlayerCharacterCreateScreen(
    onBackClick: () -> Unit,
    onCharacterCreated: (String) -> Unit,
    viewModel: PlayerCharacterCreateViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    PlayerCharacterForm(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
        onSaved = onCharacterCreated,
        snackbarHostState = snackbarHostState
    )
}
