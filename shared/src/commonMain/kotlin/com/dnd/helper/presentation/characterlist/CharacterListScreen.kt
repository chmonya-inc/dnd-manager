package com.dnd.helper.presentation.characterlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.Character

// Race-based colors for subtle card backgrounds
private fun getRaceColor(race: String): Color {
    return when (race.lowercase()) {
        "human" -> Color(0xFFF5F5F5)
        "elf" -> Color(0xFFE8F5E9)
        "dwarf" -> Color(0xFFEFEBE9)
        "halfling" -> Color(0xFFFFF3E0)
        "dragonborn" -> Color(0xFFFFEBEE)
        "tiefling" -> Color(0xFFF3E5F5)
        "gnome" -> Color(0xFFE0F2F1)
        "half-orc" -> Color(0xFFF1F8E9)
        else -> Color(0xFFECEFF1)
    }
}

@Composable
fun CharacterListScreen(
    onCharacterClick: (String) -> Unit,
    onCreateCharacter: () -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    viewModel: CharacterListViewModel = org.koin.compose.viewmodel.koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    // Start auto-refresh polling when the screen is visible,
    // stop when the user navigates away.
    DisposableEffect(viewModel) {
        viewModel.startAutoRefresh()
        onDispose { viewModel.stopAutoRefresh() }
    }

    CharacterListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onCharacterClick = onCharacterClick,
        onCreateCharacter = onCreateCharacter,
        modifier = modifier,
        showTopBar = showTopBar,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterListContent(
    state: CharacterListState,
    onEvent: (CharacterListEvent) -> Unit,
    onCharacterClick: (String) -> Unit,
    onCreateCharacter: () -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
) {
    val pullRefreshState = rememberPullToRefreshState()
    val isRefreshing = state.isLoading && state.characters.isNotEmpty()

    Scaffold(
        topBar = {
            if (showTopBar) {
                Surface(tonalElevation = 2.dp, shadowElevation = 2.dp) {
                    Column {
                        TopAppBar(
                            title = { 
                                Text("Characters", fontWeight = FontWeight.ExtraBold) 
                            },
                            actions = {
                                IconButton(onClick = onCreateCharacter) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Create Character",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
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
                    }
                }
            }
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
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }

                // Error on first load (no data to show yet)
                state.error != null && state.characters.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Failed to load characters",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = state.error!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { onEvent(CharacterListEvent.Refresh) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry Sync")
                        }
                    }
                }

                // Empty sheet
                state.characters.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No characters found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add your first character to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onCreateCharacter, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Create New Character")
                        }
                    }
                }

                // Normal list — pull-to-refresh is active here
                else -> {
                    val uniqueCharacters = state.characters.distinctBy { it.id }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        item {
                            Text(
                                "Your Party",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(
                            items = uniqueCharacters,
                            key = { it.id },
                        ) { character ->
                            CharacterCard(
                                character = character,
                                onClick = { 
                                    onEvent(CharacterListEvent.CharacterClicked(character.id))
                                    onCharacterClick(character.id)
                                },
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
    val raceColor = getRaceColor(character.race)
    
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            // Subtle race-colored gradient in background
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(raceColor.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp
                ) {
                    if (!character.displayImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = character.displayImageUrl,
                            contentDescription = character.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "LVL ${character.level}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(2.dp))
                    
                    Text(
                        text = "${character.race} · ${character.characterClass}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Player: ${character.playerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp).padding(end = 4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}
