package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.dnd.helper.domain.model.Note
import com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel
import com.dnd.helper.presentation.characterdetail.CharacterDetailEvent
import com.dnd.helper.presentation.characterdetail.CharacterDetailState
import com.dnd.helper.presentation.characterdetail.combat.CombatTab
import com.dnd.helper.presentation.characterdetail.features.FeaturesTab
import com.dnd.helper.presentation.characterdetail.inventory.InventoryTab
import com.dnd.helper.presentation.characterdetail.skills.SkillsTab
import com.dnd.helper.presentation.diceroll.DiceRollDialog
import org.koin.compose.viewmodel.koinViewModel

// Consistent colors
private val CharacterColor = Color(0xFFAB47BC)
private val StatsColor = Color(0xFFEF5350)
private val SkillsColor = Color(0xFF66BB6A)
private val CombatColor = Color(0xFF42A5F5)
private val InventoryColor = Color(0xFFFFA726)
private val FeaturesColor = Color(0xFFAB47BC)
private val NotesColor = Color(0xFF795548) // Brown for notes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterCharacterDetailScreen(
    viewModel: CharacterDetailViewModel,
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(viewModel) {
        viewModel.startAutoRefresh()
        onDispose {
            viewModel.flushPendingSave()
            viewModel.stopAutoRefresh()
        }
    }

    var showDiceDialog by remember { mutableStateOf(false) }

    if (showDiceDialog) {
        DiceRollDialog(onDismiss = { showDiceDialog = false })
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 2.dp) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(32.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = CharacterColor.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp), tint = CharacterColor)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(state.character?.name ?: "Character", fontWeight = FontWeight.Bold)
                            if (state.hasUnsavedChanges) {
                                IconButton(
                                    onClick = { viewModel.onEvent(CharacterDetailEvent.SaveChanges) },
                                    modifier = Modifier.size(32.dp).padding(start = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(0xFFFB8C00), CircleShape)
                                            .border(1.dp, Color.White, CircleShape)
                                    )
                                }
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
                        val presentationViewModel: PresentationViewModel = koinViewModel()
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
                        if (state.isEditing) {
                            IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.SaveChanges) }) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = Color(0xFF4CAF50))
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
                            IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.ToggleEdit) }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.Refresh) }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDiceDialog = true }) {
                Text(text = "🎲", style = MaterialTheme.typography.headlineSmall)
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
                    MasterContent(character, state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun MasterContent(
    character: com.dnd.helper.domain.model.Character,
    state: CharacterDetailState,
    viewModel: CharacterDetailViewModel
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        if (state.isEditing) {
            state.editedCharacter?.let { edited ->
                EditFields(edited, viewModel)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Left Column: Avatar and Basic Info
                Column(modifier = Modifier.width(300.dp)) {
                    if (!character.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = character.imageUrl,
                            contentDescription = character.name,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                    MasterHeader(character, viewModel)
                    Spacer(Modifier.height(24.dp))
                    MasterHealthSection(character, viewModel)
                }

                // Middle Column: Stats and Skills
                Column(modifier = Modifier.weight(1f)) {
                    ExpandableSection(title = "Core Stats", icon = Icons.Default.FitnessCenter, color = StatsColor, initialExpanded = true) {
                        MasterStatsGrid(character, viewModel)
                    }
                    Spacer(Modifier.height(16.dp))
                    ExpandableSection(title = "Skills & Proficiencies", icon = Icons.Default.AutoFixHigh, color = SkillsColor, initialExpanded = true) {
                        // Constrain height to avoid infinity constraint crash with nested LazyVerticalGrid
                        Box(modifier = Modifier.heightIn(max = 500.dp)) {
                            SkillsTab(character, viewModel::onEvent, isMasterMode = state.isMasterMode)
                        }
                    }
                }

                // Right Column: Inventory and Features
                Column(modifier = Modifier.weight(1f)) {
                    ExpandableSection(title = "Combat Status", icon = Icons.Default.SportsMartialArts, color = CombatColor, initialExpanded = false) {
                        // CombatTab internally uses a verticalScroll. 
                        Box(modifier = Modifier.heightIn(max = 500.dp)) {
                            CombatTab(character, isMasterMode = state.isMasterMode, isScrollable = false)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    ExpandableSection(title = "Equipment & Items", icon = Icons.Default.ShoppingBag, color = InventoryColor, initialExpanded = true) {
                        // Constrain height to avoid infinity constraint crash with nested LazyVerticalGrid
                        Box(modifier = Modifier.heightIn(max = 500.dp)) {
                            InventoryTab(items = character.items, onEvent = viewModel::onEvent, isMasterMode = state.isMasterMode)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    ExpandableSection(title = "Features & Traits", icon = Icons.Default.Star, color = FeaturesColor, initialExpanded = false) {
                        // FeaturesTab internally uses a verticalScroll. 
                        Box(modifier = Modifier.heightIn(max = 600.dp)) {
                            FeaturesTab(character, isMasterMode = state.isMasterMode)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            ExpandableSection(title = "Personal Notes", icon = Icons.Default.Description, color = NotesColor, initialExpanded = true) {
                MasterNotesSection(notes = character.notes, onEvent = viewModel::onEvent, isMasterMode = state.isMasterMode)
            }

            Spacer(Modifier.height(24.dp))
            ExpandableSection(title = "Biography & Notes", icon = Icons.AutoMirrored.Filled.Notes, color = MaterialTheme.colorScheme.secondary, initialExpanded = false) {
                if (state.isMasterMode) {
                    OutlinedTextField(
                        value = character.description,
                        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(character.copy(description = it))) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Biography / Notes") },
                        minLines = 5
                    )
                } else {
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
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: ImageVector,
    color: Color,
    initialExpanded: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = color.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, modifier = Modifier.size(18.dp), tint = color)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun EditFields(edited: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
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
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = edited.characterClass,
            onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(characterClass = it))) },
            label = { Text("Class") },
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = edited.level.toString(),
            onValueChange = { 
                val value = it.toIntOrNull() ?: 0
                viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(level = value))) 
            },
            label = { Text("Lvl") },
            modifier = Modifier.weight(0.5f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = edited.description,
        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(description = it))) },
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
}

@Composable
private fun MasterHeader(character: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
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
            shape = RoundedCornerShape(16.dp)
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
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
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
}

@Composable
private fun MasterHealthSection(character: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1
    val hpRatio = (character.currentHp.toFloat() / character.maxHp).coerceIn(0f, 1f)
    val hpColor = if (hpRatio <= 0.4f) Color(0xFFD32F2F) else Color(0xFFE53935)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = hpColor.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hit Points",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = hpColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${character.currentHp}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = hpColor
                    )
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "${character.maxHp}",
                        style = MaterialTheme.typography.titleMedium,
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
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = hpColor,
                trackColor = hpColor.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateHp(-amount)) }) { Icon(Icons.Default.Remove, null) }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { it.isDigit() } && it.length < 4) amountText = it },
                    modifier = Modifier.width(60.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateHp(amount)) }) { Icon(Icons.Default.Add, null) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MasterNotesSection(
    notes: List<Note>,
    onEvent: (CharacterDetailEvent) -> Unit,
    isMasterMode: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notes (${notes.size})", style = MaterialTheme.typography.titleSmall)
            Button(
                onClick = { onEvent(CharacterDetailEvent.AddNote) },
                colors = ButtonDefaults.buttonColors(containerColor = NotesColor),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", style = MaterialTheme.typography.labelMedium)
            }
        }

        if (notes.isEmpty()) {
            Text(
                "No custom notes yet", 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        } else {
            // Display notes in a grid-like flow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                notes.forEach { note ->
                    Box(modifier = Modifier.width(300.dp)) {
                        MasterNoteCard(note = note, onEvent = onEvent)
                    }
                }
            }
        }
    }
}

@Composable
private fun MasterNoteCard(
    note: Note,
    onEvent: (CharacterDetailEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
        border = BorderStroke(1.dp, NotesColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BasicTextField(
                    value = note.title,
                    onValueChange = { onEvent(CharacterDetailEvent.UpdateNote(note.copy(title = it))) },
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { onEvent(CharacterDetailEvent.RemoveNote(note.id)) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            BasicTextField(
                value = note.content,
                onValueChange = { onEvent(CharacterDetailEvent.UpdateNote(note.copy(content = it))) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
            )
        }
    }
}

@Composable
private fun MasterStatsGrid(character: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
    // Correct mapping of short labels to the property names expected by CharacterDetailViewModel
    val stats = listOf(
        Triple("STR", character.stats.strength, "strength"),
        Triple("DEX", character.stats.dexterity, "dexterity"),
        Triple("CON", character.stats.constitution, "constitution"),
        Triple("INT", character.stats.intelligence, "intelligence"),
        Triple("WIS", character.stats.wisdom, "wisdom"),
        Triple("CHA", character.stats.charisma, "charisma")
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in stats.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { j ->
                    if (i + j < stats.size) {
                        val (label, value, key) = stats[i + j]
                        Card(
                            modifier = Modifier.weight(1f),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                                Text(value.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Row {
                                    IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateStat(key, -1)) }, modifier = Modifier.size(24.dp)) { 
                                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp)) 
                                    }
                                    IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateStat(key, 1)) }, modifier = Modifier.size(24.dp)) { 
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp)) 
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
