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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
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
    val displayChar = if (state.isEditing) state.editedCharacter ?: character else character

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Left Column: Avatar and Basic Info
            Column(modifier = Modifier.width(260.dp)) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    AsyncImage(
                        model = displayChar.displayImageUrl,
                        contentDescription = displayChar.name,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    if (state.isEditing) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .size(36.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp), tint = Color.White)
                            }
                        }
                    }
                }

                if (state.isEditing) {
                    Spacer(Modifier.height(12.dp))
                    Text(displayChar.name.ifBlank { "New Character" }, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${displayChar.race} ${displayChar.characterClass} • Lvl ${displayChar.level}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text(displayChar.description.take(100) + if(displayChar.description.length > 100) "..." else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Spacer(Modifier.height(12.dp))
                    MasterHeader(character, viewModel)
                    Spacer(Modifier.height(16.dp))
                    MasterHealthSection(character, viewModel)
                    if (character.currentHp <= 0) {
                        Spacer(Modifier.height(12.dp))
                        MasterDeathSaves(character, viewModel, state.lastDeathSaveRoll)
                    }
                }
            }

            if (state.isEditing) {
                Column(modifier = Modifier.weight(2.2f)) {
                    EditFields(displayChar, state, viewModel)
                }
            } else {
                // Middle Column: Stats and Skills
                Column(modifier = Modifier.weight(1.2f)) {
                    ExpandableSection(title = "Core Stats", icon = Icons.Default.FitnessCenter, color = StatsColor, initialExpanded = true) {
                        MasterStatsGrid(character, viewModel)
                    }
                    Spacer(Modifier.height(12.dp))
                    ExpandableSection(title = "Skills & Proficiencies", icon = Icons.Default.AutoFixHigh, color = SkillsColor, initialExpanded = true) {
                        // Constrain height to avoid infinity constraint crash with nested LazyVerticalGrid
                        Box(modifier = Modifier.heightIn(max = 450.dp)) {
                            SkillsTab(character, viewModel::onEvent, isMasterMode = state.isMasterMode)
                        }
                    }
                }

                // Right Column: Inventory and Features
                Column(modifier = Modifier.weight(1f)) {
                    ExpandableSection(title = "Combat Status", icon = Icons.Default.SportsMartialArts, color = CombatColor, initialExpanded = false) {
                        // CombatTab internally uses a verticalScroll.
                        Box(modifier = Modifier.heightIn(max = 450.dp)) {
                            CombatTab(character, isMasterMode = state.isMasterMode, isScrollable = false)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    ExpandableSection(title = "Equipment & Items", icon = Icons.Default.ShoppingBag, color = InventoryColor, initialExpanded = true) {
                        // Constrain height to avoid infinity constraint crash with nested LazyVerticalGrid
                        Box(modifier = Modifier.heightIn(max = 450.dp)) {
                            InventoryTab(items = character.items, onEvent = viewModel::onEvent, isMasterMode = state.isMasterMode)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    ExpandableSection(title = "Features & Traits", icon = Icons.Default.Star, color = FeaturesColor, initialExpanded = false) {
                        // FeaturesTab internally uses a verticalScroll.
                        Box(modifier = Modifier.heightIn(max = 500.dp)) {
                            FeaturesTab(character, isMasterMode = state.isMasterMode)
                        }
                    }
                }
            }
        }

        if (!state.isEditing) {
            Spacer(Modifier.height(16.dp))
            ExpandableSection(title = "Personal Notes", icon = Icons.Default.Description, color = NotesColor, initialExpanded = true) {
                MasterNotesSection(notes = character.notes, onEvent = viewModel::onEvent, isMasterMode = state.isMasterMode)
            }

            Spacer(Modifier.height(16.dp))
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

            Spacer(Modifier.height(80.dp))
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (expanded) 1.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = color.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(icon, null, modifier = Modifier.size(16.dp), tint = color)
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, 
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                content()
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
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = edited.imageUrl ?: "",
        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(imageUrl = it.ifBlank { null }))) },
        label = { Text("Image URL") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        value = edited.description,
        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(description = it))) },
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text("AI Image Generation Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.aiPrompt,
            onValueChange = { viewModel.onEvent(CharacterDetailEvent.UpdateAiPrompt(it)) },
            label = { Text("AI Prompt") },
            modifier = Modifier.weight(1f),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )
        
        Column(
            modifier = Modifier.width(160.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.aiWidth.toString(),
                    onValueChange = { 
                        val w = it.toIntOrNull() ?: state.aiWidth
                        viewModel.onEvent(CharacterDetailEvent.UpdateAiSize(w, state.aiHeight))
                    },
                    label = { Text("W") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = state.aiHeight.toString(),
                    onValueChange = { 
                        val h = it.toIntOrNull() ?: state.aiHeight
                        viewModel.onEvent(CharacterDetailEvent.UpdateAiSize(state.aiWidth, h))
                    },
                    label = { Text("H") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
            }
            Button(
                onClick = { viewModel.onEvent(CharacterDetailEvent.GenerateImage) },
                enabled = edited.imageUrl != "url will appear after generation",
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (edited.imageUrl == "url will appear after generation") {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Icon(Icons.Default.AutoFixHigh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Generate", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun MasterHeader(character: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1

    Text(
        text = character.name,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.ExtraBold
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "${character.race} ${character.characterClass}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "LVL ${character.level}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateLevel(-amount)) },
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(10.dp))
                }
                
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = amountText,
                        onValueChange = { if (it.all { it.isDigit() } && it.length < 3) amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.labelLarge.copy(
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
                    modifier = Modifier.size(18.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(10.dp))
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
        colors = CardDefaults.cardColors(containerColor = hpColor.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hit Points",
                    style = MaterialTheme.typography.titleSmall,
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
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                    Text(
                        text = "${character.maxHp}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateMaxHp(-amount)) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp))
                    }
                    IconButton(
                        onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateMaxHp(amount)) },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { hpRatio },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = hpColor,
                trackColor = hpColor.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateHp(-amount)) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp)) }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { it.isDigit() } && it.length < 4) amountText = it },
                    modifier = Modifier.width(54.dp).height(44.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp)
                )
                IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateHp(amount)) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp)) }
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
private fun MasterDeathSaves(
    character: com.dnd.helper.domain.model.Character,
    viewModel: CharacterDetailViewModel,
    lastDeathSaveRoll: Int? = null,
) {
    val combat = character.combat
    val isStable = combat.deathSaveSuccesses >= 3
    val isDead = combat.deathSaveFailures >= 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDead -> Color(0xFFD32F2F).copy(alpha = 0.15f)
                isStable -> Color(0xFF43A047).copy(alpha = 0.15f)
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            when {
                isDead -> Color(0xFFD32F2F).copy(alpha = 0.5f)
                isStable -> Color(0xFF43A047).copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Death Saving Throws",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Successes
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Successes",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF43A047),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) { i ->
                            DeathSaveDiamond(
                                filled = i < combat.deathSaveSuccesses,
                                color = Color(0xFF43A047),
                            )
                        }
                    }
                }
                // Failures
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Failures",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD32F2F),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(3) { i ->
                            DeathSaveDiamond(
                                filled = i < combat.deathSaveFailures,
                                color = Color(0xFFD32F2F),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isStable -> Text(
                    text = "✓ Stabilized",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF43A047),
                )
                isDead -> Text(
                    text = "✗ Dead",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F),
                )
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { viewModel.onEvent(CharacterDetailEvent.RollDeathSave) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    ) {
                        Text("Roll Death Save (d20)")
                    }
                    if (lastDeathSaveRoll != null) {
                        val rollColor = if (lastDeathSaveRoll >= 10) Color(0xFF43A047) else Color(0xFFD32F2F)
                        Text(
                            text = "Rolled: $lastDeathSaveRoll",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = rollColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeathSaveDiamond(filled: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (filled) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (filled) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun MasterStatsGrid(character: com.dnd.helper.domain.model.Character, viewModel: CharacterDetailViewModel) {
    val stats = listOf(
        MasterStatData("STR", character.stats.strength, "strength", Color(0xFFE53935), Icons.Default.FitnessCenter),
        MasterStatData("DEX", character.stats.dexterity, "dexterity", Color(0xFF43A047), Icons.AutoMirrored.Filled.DirectionsRun),
        MasterStatData("CON", character.stats.constitution, "constitution", Color(0xFFFB8C00), Icons.Default.Favorite),
        MasterStatData("INT", character.stats.intelligence, "intelligence", Color(0xFF1E88E5), Icons.Default.Psychology),
        MasterStatData("WIS", character.stats.wisdom, "wisdom", Color(0xFF8E24AA), Icons.Default.Visibility),
        MasterStatData("CHA", character.stats.charisma, "charisma", Color(0xFFFDD835), Icons.Default.Star)
    )
    
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in stats.indices step 2) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) { j ->
                    if (i + j < stats.size) {
                        MasterStatCard(stats[i + j], viewModel, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private data class MasterStatData(
    val label: String,
    val value: Int,
    val key: String,
    val color: Color,
    val icon: ImageVector
)

@Composable
private fun MasterStatCard(
    stat: MasterStatData,
    viewModel: CharacterDetailViewModel,
    modifier: Modifier = Modifier
) {
    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1
    val modifierVal = (stat.value - 10) / 2
    val modifierSign = if (modifierVal >= 0) "+" else ""

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(stat.icon, null, modifier = Modifier.size(12.dp), tint = stat.color)
                Text(stat.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = stat.color)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stat.value.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    color = stat.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$modifierSign$modifierVal",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = stat.color
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateStat(stat.key, -amount)) }, modifier = Modifier.size(22.dp)) { 
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(12.dp)) 
                }
                
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = amountText,
                        onValueChange = { if (it.all { it.isDigit() } && it.length < 3) amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.labelSmall.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateStat(stat.key, amount)) }, modifier = Modifier.size(22.dp)) { 
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(12.dp))
                }
            }
        }
    }
}
