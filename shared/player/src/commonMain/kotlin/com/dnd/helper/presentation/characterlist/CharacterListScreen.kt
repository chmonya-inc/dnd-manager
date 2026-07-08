package com.dnd.helper.presentation.characterlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.dnd.helper.domain.model.Spell
import com.dnd.helper.domain.model.abilityModifier
import com.dnd.helper.theme.DndIcons

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
    showSummary: Boolean = false,
    sessionKey: String = "",
    viewModel: CharacterListViewModel = org.koin.compose.viewmodel.koinViewModel(key = sessionKey),
) {
    val state by viewModel.state.collectAsState()

    CharacterListContent(
        state = state,
        onEvent = viewModel::onEvent,
        onCharacterClick = onCharacterClick,
        onCreateCharacter = onCreateCharacter,
        modifier = modifier,
        showTopBar = showTopBar,
        showSummary = showSummary,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CharacterListContent(
    state: CharacterListState,
    onEvent: (CharacterListEvent) -> Unit,
    onCharacterClick: (String) -> Unit,
    onCreateCharacter: () -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    showSummary: Boolean = false,
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
                            shape = MaterialTheme.shapes.medium
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
                        Button(onClick = onCreateCharacter, shape = MaterialTheme.shapes.medium) {
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
                            if (showSummary) {
                                SummaryCharacterCard(
                                    character = character,
                                    onClick = {
                                        onEvent(CharacterListEvent.CharacterClicked(character.id))
                                        onCharacterClick(character.id)
                                    },
                                )
                            } else {
                                SimpleCharacterCard(
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleCharacterCard(
    character: Character,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val raceColor = getRaceColor(character.race)
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
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
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp
                ) {
                    val imageUrl = character.displayImageUrl
                    val isGenerating = imageUrl?.startsWith("generating:") == true

                    if (!imageUrl.isNullOrBlank()) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            } else {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = character.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
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
                        character.ownerUsername?.let { owner ->
                            Text(
                                text = " (@$owner)",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { 
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(character.id))
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = DndIcons.Filled.ContentCopy,
                                contentDescription = "Copy ID",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SummaryCharacterCard(
    character: Character,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hpRatio = if (character.maxHp > 0) {
        (character.currentHp.toFloat() / character.maxHp).coerceIn(0f, 1f)
    } else 0f
    val hpColor = when {
        character.currentHp <= 0 -> Color(0xFF9E9E9E)
        hpRatio <= 0.4f -> Color(0xFFD32F2F)
        else -> Color(0xFF43A047)
    }
    val isDying = character.currentHp <= 0 && character.combat.deathSaveFailures < 3
    val isDead = character.currentHp <= 0 && character.combat.deathSaveFailures >= 3
    val raceColor = getRaceColor(character.race)
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
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

            Column(modifier = Modifier.padding(12.dp)) {
                // Header: Avatar + Name + Level + AC
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        val imageUrl = character.displayImageUrl
                        val isGenerating = imageUrl?.startsWith("generating:") == true

                        if (!imageUrl.isNullOrBlank()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (isGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = character.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = character.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            character.ownerUsername?.let { owner ->
                                Text(
                                    text = " (@$owner)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                            }
                            IconButton(
                                onClick = { 
                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(character.id))
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = DndIcons.Filled.ContentCopy,
                                    contentDescription = "Copy ID",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            text = "${character.race} ${character.characterClass}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Text(
                            text = "LVL ${character.level}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Icon(
                                imageVector = DndIcons.Filled.Shield,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            Text(
                                text = "${character.combat.armorClass}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // HP bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = if (character.currentHp <= 0) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = hpColor,
                    )
                    LinearProgressIndicator(
                        progress = { hpRatio },
                        modifier = Modifier.weight(1f).height(5.dp).clip(MaterialTheme.shapes.extraSmall),
                        color = hpColor,
                        trackColor = hpColor.copy(alpha = 0.2f),
                    )
                    Text(
                        text = "${character.currentHp}/${character.maxHp}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = hpColor,
                    )
                }

                // Death saves if dying
                if (isDying) {
                    Spacer(Modifier.height(6.dp))
                    DeathSavesRow(
                        successes = character.combat.deathSaveSuccesses,
                        failures = character.combat.deathSaveFailures,
                    )
                }

                // Dead indicator
                if (isDead) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "✗ Dead",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                    )
                }

                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                // Ability modifiers
                AbilityModifiersRow(stats = character.stats)

                // Spells
                if (character.spells.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        character.spells.take(6).forEach { spell ->
                            SpellChip(spell = spell)
                        }
                        if (character.spells.size > 6) {
                            Text(
                                text = "+${character.spells.size - 6}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.CenterVertically),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AbilityModifiersRow(stats: com.dnd.helper.domain.model.CharacterStats) {
    val abilities = listOf(
        "STR" to abilityModifier(stats.strength),
        "DEX" to abilityModifier(stats.dexterity),
        "CON" to abilityModifier(stats.constitution),
        "INT" to abilityModifier(stats.intelligence),
        "WIS" to abilityModifier(stats.wisdom),
        "CHA" to abilityModifier(stats.charisma),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        abilities.forEach { (label, mod) ->
            val color = when {
                mod > 0 -> Color(0xFF43A047)
                mod < 0 -> Color(0xFFD32F2F)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (mod >= 0) "+$mod" else "$mod",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun SpellChip(spell: Spell) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.extraSmall,
    ) {
        Text(
            text = spell.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
private fun DeathSavesRow(successes: Int, failures: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) { i ->
                DeathSaveDot(filled = i < successes, color = Color(0xFF43A047))
            }
        }
        Text(
            text = "Death Saves",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) { i ->
                DeathSaveDot(filled = i < failures, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
private fun DeathSaveDot(filled: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(
                if (filled) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            ),
    )
}
