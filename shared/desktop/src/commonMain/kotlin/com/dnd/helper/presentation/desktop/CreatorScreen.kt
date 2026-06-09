package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.data.remote.PromptGenerator
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.repository.EditingRepository
import com.dnd.helper.presentation.charactercreate.CharacterCreateScreen
import com.dnd.helper.theme.LocalDndColors
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.random.Random

@Composable
fun ImageGenerationButton(
    prompt: String,
    onImageUrlGenerated: (String) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    entityId: String? = null,
    entityType: String? = null,
    generationType: GenerationType? = null,
    width: Int? = null,
    height: Int? = null
) {
    val editingRepository: EditingRepository = koinInject()
    var currentTaskId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentTaskId) {
        val taskId = currentTaskId ?: return@LaunchedEffect
        editingRepository.activeTasks.collect { tasks ->
            val task = tasks.find { it.id == taskId }
            if (task != null) {
                if (task.status == com.dnd.helper.domain.repository.GenerationStatus.COMPLETED && task.resultUrl != null) {
                    onImageUrlGenerated(task.resultUrl!!)
                    currentTaskId = null
                } else if (task.status == com.dnd.helper.domain.repository.GenerationStatus.FAILED) {
                    onImageUrlGenerated("") // Clear the "generating" state
                    currentTaskId = null
                }
            }
        }
    }

    IconButton(
        onClick = {
            if (prompt.isNotBlank() && entityId != null && entityType != null) {
                val genType = generationType ?: when (entityType.lowercase()) {
                    "character" -> GenerationType.CHARACTER
                    "npc" -> GenerationType.NPC
                    "monster" -> GenerationType.MONSTER
                    "location" -> GenerationType.LOCATION
                    "skill" -> GenerationType.SKILL
                    "item" -> GenerationType.ITEM
                    else -> GenerationType.CHARACTER
                }
                
                val mockUrl = editingRepository.startGeneration(
                    entityId = entityId,
                    entityType = entityType,
                    prompt = prompt,
                    genType = genType,
                    width = width ?: 1024,
                    height = height ?: 1024
                )
                currentTaskId = mockUrl.removePrefix("generating:")
                onImageUrlGenerated(mockUrl)
            }
        },
        enabled = prompt.isNotBlank() && entityId != null && currentTaskId == null,
        modifier = modifier
    ) {
        if (currentTaskId != null) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = accentColor)
        } else {
            Icon(Icons.Default.AutoFixHigh, contentDescription = "Generate Image", tint = accentColor)
        }
    }
}

sealed class CreatorType(val title: String, val icon: ImageVector) {
    data class Character(val existingCharacter: com.dnd.helper.domain.model.Character? = null) : CreatorType("Character", Icons.Default.PersonAdd)
    data class Item(val existingItem: com.dnd.helper.domain.model.Item? = null, val ownerId: String? = null) : CreatorType("Item", Icons.Default.ShoppingBag)
    data class Monster(val existingMonster: com.dnd.helper.domain.model.Monster? = null) : CreatorType("Monster", Icons.Default.BugReport)
    data class Npc(val existingNpc: com.dnd.helper.domain.model.Npc? = null) : CreatorType("NPC", Icons.Default.EmojiPeople)
    data class Location(val existingLocation: com.dnd.helper.domain.model.Location? = null) : CreatorType("Location", Icons.Default.Explore)
    data class Battlefield(val existingBattlefield: com.dnd.helper.domain.model.Battlefield? = null) : CreatorType("Battlefield", Icons.Default.Map)
}

@Composable
fun CreatorType.themeColor(): Color {
    val colors = LocalDndColors.current
    return when(this) {
        is CreatorType.Character -> colors.character
        is CreatorType.Item -> colors.item
        is CreatorType.Monster -> colors.monster
        is CreatorType.Npc -> colors.npc
        is CreatorType.Location -> colors.location
        is CreatorType.Battlefield -> colors.location
    }
}

@Composable
fun CreatorScreen(
    initialType: CreatorType? = null,
    onCreated: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedType by remember(initialType) { mutableStateOf<CreatorType?>(initialType) }

    if (selectedType == null) {
        CreatorSelection(onSelect = { selectedType = it })
    } else {
        val isEditing = when (val t = selectedType!!) {
            is CreatorType.Character -> t.existingCharacter != null
            is CreatorType.Item -> t.existingItem != null
            is CreatorType.Monster -> t.existingMonster != null
            is CreatorType.Npc -> t.existingNpc != null
            is CreatorType.Location -> t.existingLocation != null
            is CreatorType.Battlefield -> t.existingBattlefield != null
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        selectedType = null
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(selectedType!!.themeColor().copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = selectedType!!.icon,
                            contentDescription = null,
                            tint = selectedType!!.themeColor(),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (isEditing) "Edit ${selectedType!!.title}" else "Create New ${selectedType!!.title}", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (val type = selectedType!!) {
                    is CreatorType.Character -> CharacterCreateScreen(
                        existingCharacter = type.existingCharacter,
                        onBackClick = { 
                            selectedType = null
                            onBack()
                        },
                        onCharacterCreated = { 
                            selectedType = null
                            onCreated()
                        }
                    )
                    is CreatorType.Location -> LocationCreateForm(
                        existing = type.existingLocation,
                        onBackClick = { 
                            selectedType = null
                            onBack()
                        },
                        onCreated = { 
                            selectedType = null
                            onCreated()
                        }
                    )
                    is CreatorType.Battlefield -> BattlefieldCreateForm(
                        existing = type.existingBattlefield,
                        onBackClick = { 
                            selectedType = null
                            onBack()
                        },
                        onCreated = { 
                            selectedType = null
                            onCreated()
                        }
                    )
                    is CreatorType.Monster -> com.dnd.helper.presentation.monstercreate.MonsterCreateScreen(
                        initialMonster = type.existingMonster,
                        onBackClick = { 
                            selectedType = null
                            onBack()
                        },
                        onMonsterCreated = { 
                            selectedType = null
                            onCreated()
                        }
                    )
                    is CreatorType.Item -> com.dnd.helper.presentation.itemcreate.ItemCreateScreen(
                        existingItem = type.existingItem,
                        ownerId = type.ownerId,
                        viewModel = koinInject(),
                        onNavigateBack = { 
                            selectedType = null
                            onBack()
                        }
                    )
                    is CreatorType.Npc -> NpcCreateForm(
                        existing = type.existingNpc,
                        onBackClick = { 
                            selectedType = null
                            onBack()
                        },
                        onCreated = { 
                            selectedType = null
                            onCreated()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreatorFormLayout(
    accentColor: Color,
    isSaving: Boolean,
    saveButtonText: String,
    saveEnabled: Boolean,
    onSave: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        content()
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = onSave,
            enabled = saveEnabled && !isSaving,
            modifier = Modifier.align(Alignment.End).height(48.dp).widthIn(min = 160.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = MaterialTheme.shapes.medium
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text(saveButtonText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(16.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = color.copy(alpha = 0.2f))
    }
}

@Composable
private fun NpcCreateForm(
    existing: Npc? = null,
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()

    val npcId = remember { existing?.id ?: "npc-${Random.nextLong(1000000, 9999999)}" }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var background by remember { mutableStateOf(existing?.background ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }
    var customPrompt by remember { mutableStateOf(existing?.let { PromptGenerator.getFullPrompt("${it.name}, ${it.background}. ${it.description}", GenerationType.NPC) } ?: "") }
    var aiWidth by remember { mutableStateOf("1024") }
    var aiHeight by remember { mutableStateOf("1024") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(name, background, description) {
        customPrompt = PromptGenerator.getFullPrompt("$name, $background. $description".trim(), GenerationType.NPC)
    }

    val colors = LocalDndColors.current
    CreatorFormLayout(
        accentColor = colors.npc,
        isSaving = isSaving,
        saveButtonText = if (existing != null) "Update NPC" else "Create NPC",
        saveEnabled = name.isNotBlank(),
        onSave = {
            isSaving = true
            scope.launch {
                val npc = Npc(
                    id = npcId,
                    name = name,
                    background = background,
                    description = description,
                    imageUrl = imageUrl.ifBlank { null }
                )
                repository.saveNpc(npc)
                isSaving = false
                onCreated()
            }
        }
    ) {
        SectionHeader(Icons.Default.Person, "Identity", colors.npc)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("NPC Name *") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )
            OutlinedTextField(
                value = background,
                onValueChange = { background = it },
                label = { Text("Background / Role") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium
            )
        }
        
        SectionHeader(Icons.Default.Image, "Appearance", colors.npc)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("https://...") }
            )
            OutlinedTextField(value = aiWidth, onValueChange = { aiWidth = it }, label = { Text("W") }, modifier = Modifier.width(70.dp), shape = MaterialTheme.shapes.small, singleLine = true)
            OutlinedTextField(value = aiHeight, onValueChange = { aiHeight = it }, label = { Text("H") }, modifier = Modifier.width(70.dp), shape = MaterialTheme.shapes.small, singleLine = true)
            ImageGenerationButton(
                prompt = customPrompt.ifBlank { "NPC: $name, $background. $description" },
                onImageUrlGenerated = { imageUrl = it },
                accentColor = colors.npc,
                entityId = npcId,
                entityType = "npc",
                width = aiWidth.toIntOrNull(),
                height = aiHeight.toIntOrNull()
            )
        }
        
        SectionHeader(Icons.AutoMirrored.Filled.Notes, "Backstory & Notes", colors.npc)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / Personality / Plot Hooks") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8,
            shape = MaterialTheme.shapes.medium
        )

        SectionHeader(Icons.Default.AutoFixHigh, "AI Image Generation Prompt", colors.npc)
        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            label = { Text("AI Prompt") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = MaterialTheme.shapes.medium,
            placeholder = { Text("Detailed description for AI generation...") }
        )
    }
}

@Composable
private fun LocationCreateForm(
    existing: Location? = null,
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()

    val locationId = remember { existing?.id ?: "loc-${Random.nextLong(1000000, 9999999)}" }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }
    var customPrompt by remember { mutableStateOf(existing?.let { PromptGenerator.getFullPrompt("${it.name}. ${it.description}", GenerationType.LOCATION) } ?: "") }
    var aiWidth by remember { mutableStateOf("2048") }
    var aiHeight by remember { mutableStateOf("2048") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(name, description) {
        customPrompt = PromptGenerator.getFullPrompt("$name. $description".trim(), GenerationType.LOCATION)
    }

    val colors = LocalDndColors.current
    CreatorFormLayout(
        accentColor = colors.location,
        isSaving = isSaving,
        saveButtonText = if (existing != null) "Update Location" else "Create Location",
        saveEnabled = name.isNotBlank(),
        onSave = {
            isSaving = true
            scope.launch {
                val location = Location(
                    id = locationId,
                    name = name,
                    description = description,
                    imageUrl = imageUrl
                )
                repository.saveLocation(location)
                isSaving = false
                onCreated()
            }
        }
    ) {
        SectionHeader(Icons.Default.Map, "Identity", colors.location)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Location Name *") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        
        SectionHeader(Icons.Default.Image, "Media", colors.location)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("https://...") }
            )
            OutlinedTextField(value = aiWidth, onValueChange = { aiWidth = it }, label = { Text("W") }, modifier = Modifier.width(70.dp), shape = MaterialTheme.shapes.small, singleLine = true)
            OutlinedTextField(value = aiHeight, onValueChange = { aiHeight = it }, label = { Text("H") }, modifier = Modifier.width(70.dp), shape = MaterialTheme.shapes.small, singleLine = true)
            ImageGenerationButton(
                prompt = customPrompt.ifBlank { "Location: $name. $description" },
                onImageUrlGenerated = { imageUrl = it },
                accentColor = colors.location,
                entityId = locationId,
                entityType = "location",
                width = aiWidth.toIntOrNull(),
                height = aiHeight.toIntOrNull()
            )
        }
        
        SectionHeader(Icons.AutoMirrored.Filled.Notes, "Description", colors.location)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("History, Inhabitants, Atmosphere...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8,
            shape = MaterialTheme.shapes.medium
        )

        SectionHeader(Icons.Default.AutoFixHigh, "AI Image Generation Prompt", colors.location)
        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            label = { Text("AI Prompt") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = MaterialTheme.shapes.medium,
            placeholder = { Text("Detailed description for AI generation...") }
        )
    }
}

@Composable
private fun CreatorSelection(onSelect: (CreatorType) -> Unit) {
    val types = listOf(
        CreatorType.Character(),
        CreatorType.Item(),
        CreatorType.Monster(),
        CreatorType.Npc(),
        CreatorType.Location(),
        CreatorType.Battlefield()
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Text(
                "Dungeon Master Toolkit", 
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Choose what to weave into your world next", 
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(48.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                types.forEach { type ->
                    CreatorCard(type, onClick = { onSelect(type) })
                }
            }
        }
    }
}

@Composable
private fun CreatorCard(type: CreatorType, onClick: () -> Unit) {
    var hovered by remember { mutableStateOf(false) }
    
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier
            .size(width = 180.dp, height = 220.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (hovered) type.themeColor().copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 12.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.large,
                color = type.themeColor().copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = type.themeColor()
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                type.title, 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = type.themeColor()
            )
            Text(
                "New", 
                style = MaterialTheme.typography.labelMedium,
                color = type.themeColor().copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun BattlefieldCreateForm(
    existing: Battlefield? = null,
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()

    val battlefieldId = remember { existing?.id ?: "bf-${Random.nextLong(1000000, 9999999)}" }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }
    var customPrompt by remember { mutableStateOf(existing?.let { PromptGenerator.getFullPrompt("${it.name}. ${it.description}", GenerationType.BATTLEFIELD) } ?: "") }
    var aiWidth by remember { mutableStateOf("2048") }
    var aiHeight by remember { mutableStateOf("2048") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(name, description) {
        customPrompt = PromptGenerator.getFullPrompt("$name. $description".trim(), GenerationType.BATTLEFIELD)
    }

    val colors = LocalDndColors.current
    CreatorFormLayout(
        accentColor = colors.location, // Share color with Location
        isSaving = isSaving,
        saveButtonText = if (existing != null) "Update Battlefield" else "Create Battlefield",
        saveEnabled = name.isNotBlank(),
        onSave = {
            isSaving = true
            scope.launch {
                val battlefield = Battlefield(
                    id = battlefieldId,
                    name = name,
                    description = description,
                    imageUrl = imageUrl
                )
                repository.saveBattlefield(battlefield)
                isSaving = false
                onCreated()
            }
        }
    ) {
        SectionHeader(Icons.Default.Map, "Identity", colors.location)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Battlefield Name *") },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        )
        
        SectionHeader(Icons.Default.Image, "Media", colors.location)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                placeholder = { Text("https://...") }
            )
            OutlinedTextField(value = aiWidth, onValueChange = { aiWidth = it }, label = { Text("W") }, modifier = Modifier.width(70.dp), shape = MaterialTheme.shapes.small, singleLine = true)
            OutlinedTextField(value = aiHeight, onValueChange = { aiHeight = it }, label = { Text("H") }, modifier = Modifier.width(70.dp), shape = MaterialTheme.shapes.small, singleLine = true)
            ImageGenerationButton(
                prompt = customPrompt.ifBlank { "Battlefield: $name. $description" },
                onImageUrlGenerated = { imageUrl = it },
                accentColor = colors.location,
                entityId = battlefieldId,
                entityType = "battlefield",
                width = aiWidth.toIntOrNull(),
                height = aiHeight.toIntOrNull()
            )
        }
        
        SectionHeader(Icons.AutoMirrored.Filled.Notes, "Description", colors.location)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Tactical features, hazards, atmosphere...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8,
            shape = MaterialTheme.shapes.medium
        )

        SectionHeader(Icons.Default.AutoFixHigh, "AI Image Generation Prompt", colors.location)
        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            label = { Text("Prompt (Edit to customize generation)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = MaterialTheme.shapes.medium
        )
    }
}
