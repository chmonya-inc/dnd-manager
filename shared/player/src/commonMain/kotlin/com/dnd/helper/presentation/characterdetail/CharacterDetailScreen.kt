package com.dnd.helper.presentation.characterdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dnd.helper.di.isDesktop
import com.dnd.helper.presentation.characterdetail.combat.CombatTab
import com.dnd.helper.presentation.characterdetail.features.FeaturesTab
import com.dnd.helper.presentation.characterdetail.inventory.InventoryTab
import com.dnd.helper.presentation.characterdetail.overview.OverviewTab
import com.dnd.helper.presentation.characterdetail.spells.SpellsTab
import com.dnd.helper.presentation.characterdetail.stats.StatsTab
import com.dnd.helper.presentation.diceroll.DiceRollDialog
import com.dnd.helper.theme.DndIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    viewModel: CharacterDetailViewModel,
    onBackClick: () -> Unit,
    showBackButton: Boolean = !isDesktop,
    onPresentClick: ((com.dnd.helper.domain.model.Character) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    // Ensures any pending debounced save is flushed before leaving the screen
    // so the user doesn't lose rapid stat/HP/level clicks.
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.flushPendingSave()
        }
    }

    var showDiceDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDiceDialog) {
        DiceRollDialog(onDismiss = { showDiceDialog = false })
    }

    if (showDeleteDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Character") },
            text = {
                Text(
                    "Are you sure you want to delete \"${state.character?.name}\"? This will also remove it from all campaigns."
                )
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onEvent(CharacterDetailEvent.DeleteCharacter)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Navigate back after deletion
    if (state.character == null && !state.isLoading) {
        LaunchedEffect(Unit) { onBackClick() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(state.character?.name ?: "Character")
                        if (state.hasUnsavedChanges) {
                            IconButton(
                                onClick = { viewModel.onEvent(CharacterDetailEvent.SaveChanges) },
                                modifier = Modifier.size(32.dp).padding(start = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFFFB8C00), MaterialTheme.shapes.extraSmall)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(horizontal = 8.dp).size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (isDesktop) {
                        if (onPresentClick != null) {
                            IconButton(onClick = {
                                state.character?.let { onPresentClick(it) }
                            }) {
                                Icon(imageVector = DndIcons.Filled.Tv, contentDescription = "Present")
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.ToggleMasterMode) }) {
                            Icon(
                                imageVector = if (state.isMasterMode) DndIcons.Filled.LockOpen else DndIcons.Filled.Lock,
                                contentDescription = "Master Mode",
                                tint = if (state.isMasterMode) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                    if (state.isEditing) {
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.SaveChanges) }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                        }
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.ToggleEdit) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                        }
                    } else {
                        if (state.hasUnsavedChanges) {
                            IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.SaveChanges) }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save Pending",
                                    tint = Color(0xFFFB8C00)
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.Refresh) }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDiceDialog = true }) {
                Icon(
                    imageVector = DndIcons.Filled.Casino,
                    contentDescription = "Roll Dice"
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(DndIcons.Filled.Shield, null) },
                    label = { Text("Overview") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(DndIcons.Filled.FitnessCenter, null) },
                    label = { Text("Stats") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(DndIcons.Filled.ShoppingBag, null) },
                    label = { Text("Inventory") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(DndIcons.Filled.SportsMartialArts, null) },
                    label = { Text("Combat") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(DndIcons.Filled.AutoFixHigh, null) },
                    label = { Text("Spells") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, null) },
                    label = { Text("Features") },
                    selected = selectedTab == 5,
                    onClick = { selectedTab = 5 }
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            } else {
                state.character?.let { character ->
                    val effectiveMasterMode = if (isDesktop) state.isMasterMode else false

                    when (selectedTab) {
                        0 -> OverviewTab(character, viewModel::onEvent, state.lastDeathSaveRoll)
                        1 -> StatsTab(character)
                        2 -> InventoryTab(
                            items = character.items,
                            onEvent = viewModel::onEvent,
                            isMasterMode = effectiveMasterMode
                        )
                        3 -> CombatTab(character, isMasterMode = effectiveMasterMode)
                        4 -> SpellsTab(
                            character.spells,
                            onEvent = viewModel::onEvent,
                            isMasterMode = effectiveMasterMode
                        )
                        5 -> FeaturesTab(character, isMasterMode = effectiveMasterMode)
                    }
                }
            }
        }
    }
}
