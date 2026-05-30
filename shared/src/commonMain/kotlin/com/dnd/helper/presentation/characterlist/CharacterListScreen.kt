package com.dnd.helper.presentation.characterlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.Character

@Composable
fun CharacterListScreen(
    onCharacterClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharacterListViewModel = org.koin.compose.viewmodel.koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    CharacterListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onCharacterClick = onCharacterClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterListContent(
    state: CharacterListState,
    onEvent: (CharacterListEvent) -> Unit,
    onCharacterClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pullRefreshState = rememberPullToRefreshState()
    val isRefreshing = state.isLoading && state.characters.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Characters") },
                actions = {
                    IconButton(
                        onClick = { onEvent(CharacterListEvent.Refresh) },
                        enabled = !state.isLoading,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                        )
                    }
                },
            )
        },
        modifier = modifier
            .onPreviewKeyEvent { event ->
                if (event.key == Key.F5 && event.type == KeyEventType.KeyUp) {
                    onEvent(CharacterListEvent.Refresh)
                    true
                } else {
                    false
                }
            },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { onEvent(CharacterListEvent.Refresh) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                // Initial load — fullscreen spinner so user has something to look at
                state.isLoading && state.characters.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Error on first load (no data to show yet)
                state.error != null && state.characters.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Error: ${state.error}",
                            color = MaterialTheme.colorScheme.error,
                        )
                        Button(
                            onClick = { onEvent(CharacterListEvent.Refresh) },
                        ) {
                            Text("Retry")
                        }
                    }
                }

                // Empty sheet
                state.characters.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No characters found",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }

                // Normal list — pull-to-refresh is active here
                else -> {
                    val uniqueCharacters = state.characters.distinctBy { it.id }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = uniqueCharacters,
                            key = { it.id },
                        ) { character ->
                            CharacterCard(
                                character = character,
                                onClick = { onCharacterClick(character.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterCard(
    character: Character,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (character.imageUrl != null) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = character.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${character.race} ${character.characterClass} · Lvl ${character.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Player: ${character.playerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
