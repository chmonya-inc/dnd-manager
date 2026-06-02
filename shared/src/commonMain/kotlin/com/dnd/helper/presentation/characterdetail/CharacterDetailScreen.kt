package com.dnd.helper.presentation.characterdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.presentation.characterdetail.combat.CombatTab
import com.dnd.helper.di.isDesktop
import com.dnd.helper.presentation.characterdetail.features.FeaturesTab
import com.dnd.helper.presentation.characterdetail.inventory.InventoryTab
import com.dnd.helper.presentation.characterdetail.overview.OverviewTab
import com.dnd.helper.presentation.characterdetail.skills.SkillsTab
import com.dnd.helper.presentation.characterdetail.stats.StatsTab
import com.dnd.helper.presentation.diceroll.DiceRollDialog
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    viewModel: CharacterDetailViewModel,
    onBackClick: () -> Unit,
    showBackButton: Boolean = !isDesktop,
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

    if (showDiceDialog) {
        DiceRollDialog(onDismiss = { showDiceDialog = false })
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
                        val presentationViewModel: com.dnd.helper.presentation.desktop.PresentationViewModel = koinViewModel()
                        IconButton(onClick = {
                            state.character?.let { presentationViewModel.addItem(it.name, type = "Character", imageUrl = it.displayImageUrl) }
                        }) {
                            Icon(imageVector = Icons.Default.Tv, contentDescription = "Present")
                        }
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.ToggleMasterMode) }) {
                            Icon(
                                imageVector = if (state.isMasterMode) Icons.Default.LockOpen else Icons.Default.Lock,
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
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDiceDialog = true }) {
                Text(
                    text = "🎲",
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Shield, null) },
                    label = { Text("Overview") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.FitnessCenter, null) },
                    label = { Text("Stats") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingBag, null) },
                    label = { Text("Inventory") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.SportsMartialArts, null) },
                    label = { Text("Combat") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, null) },
                    label = { Text("Features") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AutoFixHigh, null) },
                    label = { Text("Skills") },
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
                    when (selectedTab) {
                        0 -> OverviewTab(character, viewModel::onEvent, state.lastDeathSaveRoll)
                        1 -> StatsTab(character)
                        2 -> InventoryTab(items = character.items, onEvent = viewModel::onEvent, isMasterMode = state.isMasterMode)
                        3 -> CombatTab(character, isMasterMode = state.isMasterMode)
                        4 -> FeaturesTab(character, onEvent = viewModel::onEvent, isMasterMode = state.isMasterMode)
                        5 -> SkillsTab(character, isMasterMode = state.isMasterMode)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditFields(edited: com.dnd.helper.domain.model.Character, state: CharacterDetailState, viewModel: CharacterDetailViewModel) {
    OutlinedTextField(
        value = edited.name,
        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(name = it))) },
        label = { Text("Name") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = edited.race,
            onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(race = it))) },
            label = { Text("Race") },
            modifier = Modifier.fillMaxWidth(0.33f)
        )
        OutlinedTextField(
            value = edited.characterClass,
            onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(characterClass = it))) },
            label = { Text("Class") },
            modifier = Modifier.fillMaxWidth(0.33f)
        )
        OutlinedTextField(
            value = edited.level.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(level = value)))
            },
            label = { Text("Lvl") },
            modifier = Modifier.fillMaxWidth(0.33f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = edited.currentHp.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(currentHp = value)))
            },
            label = { Text("Cur HP") },
            modifier = Modifier.fillMaxWidth(0.5f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = edited.maxHp.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(maxHp = value)))
            },
            label = { Text("Max HP") },
            modifier = Modifier.fillMaxWidth(0.5f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text("Characteristics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = edited.stats.strength.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(stats = edited.stats.copy(strength = value))))
            },
            label = { Text("STR") },
            modifier = Modifier.fillMaxWidth(0.33f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = edited.stats.dexterity.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(stats = edited.stats.copy(dexterity = value))))
            },
            label = { Text("DEX") },
            modifier = Modifier.fillMaxWidth(0.33f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = edited.stats.constitution.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(stats = edited.stats.copy(constitution = value))))
            },
            label = { Text("CON") },
            modifier = Modifier.fillMaxWidth(0.33f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = edited.stats.intelligence.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(stats = edited.stats.copy(intelligence = value))))
            },
            label = { Text("INT") },
            modifier = Modifier.fillMaxWidth(0.33f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = edited.stats.wisdom.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(stats = edited.stats.copy(wisdom = value))))
            },
            label = { Text("WIS") },
            modifier = Modifier.fillMaxWidth(0.33f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = edited.stats.charisma.toString(),
            onValueChange = {
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(stats = edited.stats.copy(charisma = value))))
            },
            label = { Text("CHA") },
            modifier = Modifier.fillMaxWidth(0.33f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = edited.playerName,
        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(playerName = it))) },
        label = { Text("Player Name") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = edited.imageUrl ?: "",
        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(imageUrl = it.ifBlank { null }))) },
        label = { Text("Image URL") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = edited.description,
        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(description = it))) },
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text("AI Image Generation Prompt (${state.aiWidth}x${state.aiHeight})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.aiPrompt,
            onValueChange = { viewModel.onEvent(CharacterDetailEvent.UpdateAiPrompt(it)) },
            label = { Text("AI Prompt") },
            modifier = Modifier.weight(1f),
            minLines = 3,
            shape = MaterialTheme.shapes.medium
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.aiWidth.toString(),
                onValueChange = { 
                    val w = it.toIntOrNull() ?: state.aiWidth
                    viewModel.onEvent(CharacterDetailEvent.UpdateAiSize(w, state.aiHeight))
                },
                label = { Text("W") },
                modifier = Modifier.width(80.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.small
            )
            OutlinedTextField(
                value = state.aiHeight.toString(),
                onValueChange = { 
                    val h = it.toIntOrNull() ?: state.aiHeight
                    viewModel.onEvent(CharacterDetailEvent.UpdateAiSize(state.aiWidth, h))
                },
                label = { Text("H") },
                modifier = Modifier.width(80.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.small
            )
            IconButton(
                onClick = { viewModel.onEvent(CharacterDetailEvent.GenerateImage) },
                enabled = edited.imageUrl != "url will appear after generation"
            ) {
                if (edited.imageUrl == "url will appear after generation") {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.AutoFixHigh, "Generate", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun CharacterHeader(character: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1

    Text(
        text = character.name,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.ExtraBold
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${character.race} ${character.characterClass}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "LVL ${character.level}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateLevel(-amount)) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(12.dp))
                }
                
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = amountText,
                        onValueChange = { if (it.all { it.isDigit() } && it.length < 3) amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                IconButton(
                    onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateLevel(amount)) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
    Text(
        text = "Player: ${character.playerName}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun HealthSection(character: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1

    val hpRatio = (character.currentHp.toFloat() / character.maxHp).coerceIn(0f, 1f)

    val hpColor = when {
        character.currentHp <= 0 -> MaterialTheme.colorScheme.onSurface
        hpRatio <= 0.4f -> Color(0xFFD32F2F)
        else -> Color(0xFFE53935)
    }

    val hpIcon = when {
        character.currentHp <= 0 -> Icons.Default.Dangerous
        hpRatio <= 0.4f -> Icons.Default.HeartBroken
        else -> Icons.Default.Favorite
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = hpColor.copy(alpha = 0.12f)
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = hpIcon,
                        contentDescription = null,
                        tint = hpColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (character.currentHp <= 0) "Dead" else "Hit Points",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = hpColor
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${character.currentHp}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = hpColor
                    )
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "${character.maxHp}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateMaxHp(-amount)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateMaxHp(amount)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { hpRatio },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(MaterialTheme.shapes.extraSmall),
                color = hpColor,
                trackColor = hpColor.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateHp(-amount)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Remove, null)
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { it.isDigit() } && it.length < 4) amountText = it },
                    modifier = Modifier.width(70.dp).height(56.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = hpColor,
                        unfocusedBorderColor = hpColor.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedIconButton(
                    onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateHp(amount)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(character: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
    val stats = listOf(
        StatData(
            label = "STR",
            value = character.stats.strength,
            key = "strength",
            tag = when {
                character.stats.strength < 8 -> "Хилый"
                character.stats.strength < 15 -> "Жилистый"
                character.stats.strength < 20 -> "Атлет"
                else -> "Геракл"
            },
            icon = when {
                character.stats.strength < 8 -> Icons.Default.Accessibility
                character.stats.strength < 15 -> Icons.Default.FitnessCenter
                character.stats.strength < 20 -> Icons.Default.SportsMartialArts
                else -> Icons.Default.Bolt
            },
            color = Color(0xFFE53935)
        ),
        StatData(
            label = "DEX",
            value = character.stats.dexterity,
            key = "dexterity",
            tag = when {
                character.stats.dexterity < 8 -> "Неуклюжий"
                character.stats.dexterity < 15 -> "Расторопный"
                character.stats.dexterity < 20 -> "Ловкач"
                else -> "Акробат"
            },
            icon = when {
                character.stats.dexterity < 8 -> Icons.AutoMirrored.Filled.DirectionsWalk
                character.stats.dexterity < 15 -> Icons.AutoMirrored.Filled.DirectionsRun
                character.stats.dexterity < 20 -> Icons.AutoMirrored.Filled.DirectionsBike
                else -> Icons.Default.AutoAwesome
            },
            color = Color(0xFF43A047)
        ),
        StatData(
            label = "CON",
            value = character.stats.constitution,
            key = "constitution",
            tag = when {
                character.stats.constitution < 8 -> "Слабый"
                character.stats.constitution < 15 -> "Выносливый"
                character.stats.constitution < 20 -> "Живучий"
                else -> "Несокрушимый"
            },
            icon = when {
                character.stats.constitution < 8 -> Icons.Default.HeartBroken
                character.stats.constitution < 15 -> Icons.Default.Favorite
                character.stats.constitution < 20 -> Icons.Default.HealthAndSafety
                else -> Icons.Default.Shield
            },
            color = Color(0xFFFB8C00)
        ),
        StatData(
            label = "INT",
            value = character.stats.intelligence,
            key = "intelligence",
            tag = when {
                character.stats.intelligence < 8 -> "Туповатый"
                character.stats.intelligence < 15 -> "Смышленый"
                character.stats.intelligence < 20 -> "Эрудит"
                else -> "Гений"
            },
            icon = when {
                character.stats.intelligence < 8 -> Icons.AutoMirrored.Filled.MenuBook
                character.stats.intelligence < 15 -> Icons.Default.Lightbulb
                character.stats.intelligence < 20 -> Icons.Default.School
                else -> Icons.Default.Psychology
            },
            color = Color(0xFF1E88E5)
        ),
        StatData(
            label = "WIS",
            value = character.stats.wisdom,
            key = "wisdom",
            tag = when {
                character.stats.wisdom < 8 -> "Рассеянный"
                character.stats.wisdom < 15 -> "Проницательный"
                character.stats.wisdom < 20 -> "Мудрец"
                else -> "Провидец"
            },
            icon = when {
                character.stats.wisdom < 8 -> Icons.Default.Visibility
                character.stats.wisdom < 15 -> Icons.Default.Explore
                character.stats.wisdom < 20 -> Icons.Default.SelfImprovement
                else -> Icons.Default.AutoFixHigh
            },
            color = Color(0xFF8E24AA)
        ),
        StatData(
            label = "CHA",
            value = character.stats.charisma,
            key = "charisma",
            tag = when {
                character.stats.charisma < 8 -> "Угрюмый"
                character.stats.charisma < 15 -> "Притягательный"
                character.stats.charisma < 20 -> "Лидер"
                else -> "Избранник"
            },
            icon = when {
                character.stats.charisma < 8 -> Icons.Default.Face
                character.stats.charisma < 15 -> Icons.Default.Mood
                character.stats.charisma < 20 -> Icons.Default.Groups
                else -> Icons.Default.Star
            },
            color = Color(0xFFFDD835)
        )
    )

    // Display in 2x3 grid using Rows to avoid nested scrolling errors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (i in 0 until 3) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(stats[i * 2], viewModel, Modifier.weight(1f))
                StatCard(stats[i * 2 + 1], viewModel, Modifier.weight(1f))
            }
        }
    }
}

private data class StatData(
    val label: String,
    val value: Int,
    val key: String,
    val tag: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun StatCard(
    stat: StatData,
    viewModel: CharacterDetailViewModel,
    modifier: Modifier = Modifier
) {
    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = stat.color,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stat.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = stat.color
                )
            }
            Text(
                text = stat.value.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = stat.color
            )
            Text(
                text = stat.tag,
                style = MaterialTheme.typography.labelSmall,
                color = stat.color.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateStat(stat.key, -amount)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp))
                }

                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(32.dp)
                        .background(stat.color.copy(alpha = 0.15f), MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = amountText,
                        onValueChange = { if (it.all { it.isDigit() } && it.length < 3) amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            color = stat.color,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                IconButton(
                    onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateStat(stat.key, amount)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CharacteristicsContent(
    character: com.dnd.helper.domain.model.Character,
    state: CharacterDetailState,
    viewModel: CharacterDetailViewModel
) {
    val displayChar = if (state.isEditing) state.editedCharacter ?: character else character

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val imageUrl = displayChar.displayImageUrl
        if (!imageUrl.isNullOrBlank()) {
            val isGenerating = imageUrl.startsWith("generating:")
            Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                if (isGenerating) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = displayChar.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        onError = { imageState ->
                            println("[AsyncImage] Failed to load hero image for ${displayChar.name}: ${imageState.result.throwable}")
                        },
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            if (state.isEditing) {
                state.editedCharacter?.let { edited ->
                    EditFields(edited, state, viewModel)
                }
            } else {
                CharacterHeader(character, viewModel)

                Spacer(modifier = Modifier.height(24.dp))

                HealthSection(character, viewModel)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Characteristics",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                StatsGrid(character, viewModel)

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Biography",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
