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
import com.dnd.helper.data.remote.AiImageService
import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.data.remote.PromptGenerator
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.presentation.charactercreate.CharacterCreateScreen
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
    val aiService: AiImageService = koinInject()
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            if (prompt.isNotBlank()) {
                isGenerating = true
                onImageUrlGenerated("url will appear after generation")
                
                val genType = generationType ?: when (entityType?.lowercase()) {
                    "character" -> GenerationType.CHARACTER
                    "npc" -> GenerationType.NPC
                    "monster" -> GenerationType.MONSTER
                    "location" -> GenerationType.LOCATION
                    "skill" -> GenerationType.SKILL
                    "item" -> GenerationType.ITEM
                    else -> GenerationType.CHARACTER
                }
                
                scope.launch {
                    val url = aiService.generateImage(prompt, genType, width, height)
                    if (url != null) {
                        onImageUrlGenerated(url)
                        // If it's an existing entity, also save to repository in background
                        if (entityId != null && entityType != null) {
                            // We use repository.enqueueImageGeneration if we want to reuse its save logic,
                            // but that would generate AGAIN. So we should have a way to just SAVE the url.
                            // For now, let's just manually save it.
                            when (entityType.lowercase()) {
                                "npc" -> {
                                    val result = repository.getNpcs()
                                    if (result is com.dnd.helper.domain.common.Result.Success) {
                                        result.data.find { it.id == entityId }?.let { npc ->
                                            repository.saveNpc(npc.copy(imageUrl = url))
                                        }
                                    }
                                }
                                "monster" -> {
                                    val result = repository.getMonsters()
                                    if (result is com.dnd.helper.domain.common.Result.Success) {
                                        result.data.find { it.id == entityId }?.let { monster ->
                                            repository.saveMonster(monster.copy(imageUrl = url))
                                        }
                                    }
                                }
                                "location" -> {
                                    val result = repository.getLocations()
                                    if (result is com.dnd.helper.domain.common.Result.Success) {
                                        result.data.find { it.id == entityId }?.let { location ->
                                            repository.saveLocation(location.copy(imageUrl = url))
                                        }
                                    }
                                }
                                "character" -> {
                                    val result = repository.getCharacter(entityId)
                                    if (result is com.dnd.helper.domain.common.Result.Success) {
                                        repository.saveCharacter(result.data.copy(imageUrl = url))
                                    }
                                }
                            }
                        }
                    } else {
                        onImageUrlGenerated("") 
                    }
                    isGenerating = false
                }
            }
        },
        enabled = !isGenerating && prompt.isNotBlank(),
        modifier = modifier
    ) {
        if (isGenerating) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = accentColor, strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.AutoFixHigh, contentDescription = "Generate Image", tint = accentColor)
        }
    }
}

// Theme colors for different categories
private val MonsterColor = Color(0xFFEF5350) // Red
private val NpcColor = Color(0xFF66BB6A)     // Green
private val LocationColor = Color(0xFF42A5F5) // Blue
private val ItemColor = Color(0xFFFFA726)     // Orange
private val CharacterColor = Color(0xFFAB47BC) // Purple

sealed class CreatorType(val title: String, val icon: ImageVector, val color: Color) {
    data object Character : CreatorType("Character", Icons.Default.PersonAdd, CharacterColor)
    data class Item(val existingItem: com.dnd.helper.domain.model.Item? = null, val ownerId: String? = null) : CreatorType("Item", Icons.Default.ShoppingBag, ItemColor)
    data class Monster(val existingMonster: com.dnd.helper.domain.model.Monster? = null) : CreatorType("Monster", Icons.Default.BugReport, MonsterColor)
    data class Npc(val existingNpc: com.dnd.helper.domain.model.Npc? = null) : CreatorType("NPC", Icons.Default.EmojiPeople, NpcColor)
    data class Location(val existingLocation: com.dnd.helper.domain.model.Location? = null) : CreatorType("Location", Icons.Default.Explore, LocationColor)
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
            is CreatorType.Item -> t.existingItem != null
            is CreatorType.Monster -> t.existingMonster != null
            is CreatorType.Npc -> t.existingNpc != null
            is CreatorType.Location -> t.existingLocation != null
            else -> false
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedType!!.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = selectedType!!.icon,
                            contentDescription = null,
                            tint = selectedType!!.color,
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
                    CreatorType.Character -> CharacterCreateScreen(
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
                    is CreatorType.Monster -> MonsterCreateForm(
                        existing = type.existingMonster,
                        onBackClick = { 
                            selectedType = null
                            onBack()
                        },
                        onCreated = { 
                            selectedType = null
                            onCreated()
                        }
                    )
                    is CreatorType.Item -> ItemCreateForm(
                        existing = type.existingItem,
                        initialOwnerId = type.ownerId,
                        onBackClick = { 
                            selectedType = null
                            onBack()
                        },
                        onCreated = { 
                            selectedType = null
                            onCreated()
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
            shape = RoundedCornerShape(12.dp)
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
    
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var background by remember { mutableStateOf(existing?.background ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }
    var customPrompt by remember { mutableStateOf(existing?.let { PromptGenerator.getFullPrompt("${it.name}, ${it.background}. ${it.description}", GenerationType.NPC) } ?: "") }
    var aiWidth by remember { mutableStateOf("512") }
    var aiHeight by remember { mutableStateOf("512") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(name, background, description) {
        customPrompt = PromptGenerator.getFullPrompt("$name, $background. $description".trim(), GenerationType.NPC)
    }

    CreatorFormLayout(
        accentColor = NpcColor,
        isSaving = isSaving,
        saveButtonText = if (existing != null) "Update NPC" else "Create NPC",
        saveEnabled = name.isNotBlank(),
        onSave = {
            isSaving = true
            scope.launch {
                val npc = Npc(
                    id = existing?.id ?: "npc-${Random.nextLong()}",
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
        SectionHeader(Icons.Default.Person, "Identity", NpcColor)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("NPC Name *") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = background,
                onValueChange = { background = it },
                label = { Text("Background / Role") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        SectionHeader(Icons.Default.Image, "Appearance", NpcColor)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("https://...") }
            )
            OutlinedTextField(value = aiWidth, onValueChange = { aiWidth = it }, label = { Text("W") }, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(8.dp), singleLine = true)
            OutlinedTextField(value = aiHeight, onValueChange = { aiHeight = it }, label = { Text("H") }, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(8.dp), singleLine = true)
            ImageGenerationButton(
                prompt = customPrompt.ifBlank { "NPC: $name, $background. $description" },
                onImageUrlGenerated = { imageUrl = it },
                accentColor = NpcColor,
                entityId = existing?.id,
                entityType = "npc",
                width = aiWidth.toIntOrNull(),
                height = aiHeight.toIntOrNull()
            )
        }
        
        SectionHeader(Icons.AutoMirrored.Filled.Notes, "Backstory & Notes", NpcColor)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / Personality / Plot Hooks") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8,
            shape = RoundedCornerShape(12.dp)
        )

        SectionHeader(Icons.Default.AutoFixHigh, "AI Image Generation Prompt", NpcColor)
        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            label = { Text("AI Prompt") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Detailed description for AI generation...") }
        )
    }
}

@Composable
private fun MonsterCreateForm(
    existing: Monster? = null,
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }
    var cr by remember { mutableStateOf(existing?.challengeRating ?: "1") }
    var type by remember { mutableStateOf(existing?.type ?: "Humanoid") }
    var alignment by remember { mutableStateOf(existing?.alignment ?: "Neutral") }
    var size by remember { mutableStateOf(existing?.size ?: "Medium") }
    var hp by remember { mutableStateOf(existing?.maxHp?.toString() ?: "10") }
    var ac by remember { mutableStateOf(existing?.armorClass?.toString() ?: "10") }
    var speed by remember { mutableStateOf(existing?.speed?.toString() ?: "30") }
    
    var str by remember { mutableStateOf(existing?.stats?.strength?.toString() ?: "10") }
    var dex by remember { mutableStateOf(existing?.stats?.dexterity?.toString() ?: "10") }
    var con by remember { mutableStateOf(existing?.stats?.constitution?.toString() ?: "10") }
    var int by remember { mutableStateOf(existing?.stats?.intelligence?.toString() ?: "10") }
    var wis by remember { mutableStateOf(existing?.stats?.wisdom?.toString() ?: "10") }
    var cha by remember { mutableStateOf(existing?.stats?.charisma?.toString() ?: "10") }
    
    var customPrompt by remember { mutableStateOf(existing?.let { PromptGenerator.getFullPrompt("${it.name}, ${it.type}, ${it.size}. ${it.description}", GenerationType.MONSTER) } ?: "") }
    var aiWidth by remember { mutableStateOf("512") }
    var aiHeight by remember { mutableStateOf("512") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(name, type, size, description) {
        customPrompt = PromptGenerator.getFullPrompt("$name, $type, $size. $description".trim(), GenerationType.MONSTER)
    }

    CreatorFormLayout(
        accentColor = MonsterColor,
        isSaving = isSaving,
        saveButtonText = if (existing != null) "Update Monster" else "Create Monster",
        saveEnabled = name.isNotBlank(),
        onSave = {
            isSaving = true
            scope.launch {
                val monster = Monster(
                    id = existing?.id ?: "monster-${Random.nextLong()}",
                    name = name,
                    description = description,
                    imageUrl = imageUrl.ifBlank { null },
                    challengeRating = cr,
                    type = type,
                    alignment = alignment,
                    size = size,
                    maxHp = hp.toIntOrNull() ?: 10,
                    currentHp = hp.toIntOrNull() ?: 10,
                    armorClass = ac.toIntOrNull() ?: 10,
                    speed = speed.toIntOrNull() ?: 30,
                    stats = CharacterStats(
                        strength = str.toIntOrNull() ?: 10,
                        dexterity = dex.toIntOrNull() ?: 10,
                        constitution = con.toIntOrNull() ?: 10,
                        intelligence = int.toIntOrNull() ?: 10,
                        wisdom = wis.toIntOrNull() ?: 10,
                        charisma = cha.toIntOrNull() ?: 10
                    )
                )
                repository.saveMonster(monster)
                isSaving = false
                onCreated()
            }
        }
    ) {
        SectionHeader(Icons.Default.Info, "General", MonsterColor)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Monster Name *") }, modifier = Modifier.weight(2f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = cr, onValueChange = { cr = it }, label = { Text("CR") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = alignment, onValueChange = { alignment = it }, label = { Text("Alignment") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Size") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        SectionHeader(Icons.Default.Shield, "Combat Stats", MonsterColor)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = hp, onValueChange = { hp = it }, label = { Text("Max HP") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = ac, onValueChange = { ac = it }, label = { Text("AC") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = speed, onValueChange = { speed = it }, label = { Text("Speed (ft)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }

        SectionHeader(Icons.Default.FitnessCenter, "Ability Scores", MonsterColor)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("STR" to str to {v:String -> str=v}, "DEX" to dex to {v:String -> dex=v}, "CON" to con to {v:String -> con=v}, 
                   "INT" to int to {v:String -> int=v}, "WIS" to wis to {v:String -> wis=v}, "CHA" to cha to {v:String -> cha=v}).forEach { (data, setter) ->
                val (label, value) = data
                OutlinedTextField(
                    value = value, 
                    onValueChange = setter, 
                    label = { Text(label) }, 
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center)
                )
            }
        }

        SectionHeader(Icons.Default.Image, "Media", MonsterColor)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(value = aiWidth, onValueChange = { aiWidth = it }, label = { Text("W") }, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(8.dp), singleLine = true)
            OutlinedTextField(value = aiHeight, onValueChange = { aiHeight = it }, label = { Text("H") }, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(8.dp), singleLine = true)
            ImageGenerationButton(
                prompt = customPrompt.ifBlank { "Monster: $name, $type, $size. $description" },
                onImageUrlGenerated = { imageUrl = it },
                accentColor = MonsterColor,
                entityId = existing?.id,
                entityType = "monster",
                width = aiWidth.toIntOrNull(),
                height = aiHeight.toIntOrNull()
            )
        }

        SectionHeader(Icons.AutoMirrored.Filled.List, "Traits & Actions", MonsterColor)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Attacks, Special Traits, Legendary Actions...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8,
            shape = RoundedCornerShape(12.dp)
        )

        SectionHeader(Icons.Default.AutoFixHigh, "AI Image Generation Prompt", MonsterColor)
        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            label = { Text("AI Prompt") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Detailed description for AI generation...") }
        )
    }
}

@Composable
private fun ItemCreateForm(
    existing: Item? = null,
    initialOwnerId: String? = null,
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var characters by remember { mutableStateOf<List<com.dnd.helper.domain.model.Character>>(emptyList()) }
    var selectedCharId by remember { mutableStateOf(initialOwnerId ?: "") }
    
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }
    var rarity by remember { mutableStateOf(existing?.rarity ?: ItemRarity.COMMON) }
    var slot by remember { mutableStateOf<EquipmentSlot?>(existing?.slot ?: EquipmentSlot.MAIN_HAND) }
    var customPrompt by remember { mutableStateOf(existing?.let { PromptGenerator.getFullPrompt("${it.name}, ${it.rarity}. ${it.description}", GenerationType.ITEM) } ?: "") }
    var aiWidth by remember { mutableStateOf("256") }
    var aiHeight by remember { mutableStateOf("256") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(name, rarity, description) {
        customPrompt = PromptGenerator.getFullPrompt("$name, $rarity. $description".trim(), GenerationType.ITEM)
    }

    LaunchedEffect(Unit) {
        val result = repository.getCharacters()
        if (result is com.dnd.helper.domain.common.Result.Success) {
            characters = result.data.filter { it.id.isNotBlank() }.distinctBy { it.id.trim() }
            if (selectedCharId.isEmpty()) selectedCharId = characters.firstOrNull()?.id ?: ""
        }
    }

    CreatorFormLayout(
        accentColor = ItemColor,
        isSaving = isSaving,
        saveButtonText = if (existing != null) "Update Item" else "Create and Assign Item",
        saveEnabled = name.isNotBlank() && selectedCharId.isNotBlank(),
        onSave = {
            isSaving = true
            scope.launch {
                val item = Item(
                    id = existing?.id ?: "item-${Random.nextLong()}",
                    name = name,
                    description = description,
                    imageUrl = imageUrl.ifBlank { null },
                    rarity = rarity,
                    slot = slot,
                    equipped = existing?.equipped ?: false,
                    stats = existing?.stats ?: emptyMap()
                )
                
                val char = characters.find { it.id == selectedCharId }
                if (char != null) {
                    val newItems = if (existing != null) {
                        char.items.map { if (it.id == existing.id) item else it }
                    } else {
                        char.items + item
                    }
                    repository.saveCharacter(char.copy(items = newItems))
                }
                
                isSaving = false
                onCreated()
            }
        }
    ) {
        SectionHeader(Icons.Default.Person, "Owner", ItemColor)
        if (existing == null) {
            Text("Select Character to receive this item:", style = MaterialTheme.typography.bodyMedium)
            if (characters.isEmpty()) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState()).padding(8.dp)) {
                        characters.forEach { char ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (selectedCharId == char.id) ItemColor.copy(alpha = 0.1f) else Color.Transparent)
                            ) {
                                RadioButton(selected = selectedCharId == char.id, onClick = { selectedCharId = char.id }, colors = RadioButtonDefaults.colors(selectedColor = ItemColor))
                                Text(char.name, modifier = Modifier.padding(start = 8.dp), fontWeight = if (selectedCharId == char.id) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = characters.find { it.id == selectedCharId }?.name ?: selectedCharId,
                onValueChange = {},
                readOnly = true,
                label = { Text("Owner") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )
        }

        SectionHeader(Icons.Default.Info, "General Info", ItemColor)
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            var rarityExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { rarityExpanded = true }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ItemColor)
                ) {
                    Icon(Icons.Default.Star, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Rarity: ${rarity.name}")
                }
                DropdownMenu(expanded = rarityExpanded, onDismissRequest = { rarityExpanded = false }) {
                    ItemRarity.entries.forEach { r ->
                        DropdownMenuItem(text = { Text(r.name) }, onClick = { rarity = r; rarityExpanded = false })
                    }
                }
            }
            
            var slotExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { slotExpanded = true }, 
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ItemColor)
                ) {
                    Icon(Icons.Default.Accessibility, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Slot: ${slot?.name ?: "None"}")
                }
                DropdownMenu(expanded = slotExpanded, onDismissRequest = { slotExpanded = false }) {
                    DropdownMenuItem(text = { Text("None") }, onClick = { slot = null; slotExpanded = false })
                    EquipmentSlot.entries.forEach { s ->
                        DropdownMenuItem(text = { Text(s.name) }, onClick = { slot = s; slotExpanded = false })
                    }
                }
            }
        }

        SectionHeader(Icons.Default.Image, "Visuals", ItemColor)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(value = aiWidth, onValueChange = { aiWidth = it }, label = { Text("W") }, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(8.dp), singleLine = true)
            OutlinedTextField(value = aiHeight, onValueChange = { aiHeight = it }, label = { Text("H") }, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(8.dp), singleLine = true)
            ImageGenerationButton(
                prompt = customPrompt.ifBlank { "Item: $name, $rarity. $description" },
                onImageUrlGenerated = { imageUrl = it },
                accentColor = ItemColor,
                entityId = existing?.id,
                entityType = "character", // Items update their owner character
                generationType = GenerationType.ITEM,
                width = aiWidth.toIntOrNull(),
                height = aiHeight.toIntOrNull()
            )
        }
        
        SectionHeader(Icons.Default.Description, "Description", ItemColor)
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Properties & Lore") }, modifier = Modifier.fillMaxWidth(), minLines = 5, shape = RoundedCornerShape(12.dp))

        SectionHeader(Icons.Default.AutoFixHigh, "AI Image Generation Prompt", ItemColor)
        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            label = { Text("AI Prompt") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
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
    
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var imageUrl by remember { mutableStateOf(existing?.imageUrl ?: "") }
    var customPrompt by remember { mutableStateOf(existing?.let { PromptGenerator.getFullPrompt("${it.name}. ${it.description}", GenerationType.LOCATION) } ?: "") }
    var aiWidth by remember { mutableStateOf("1024") }
    var aiHeight by remember { mutableStateOf("1024") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(name, description) {
        customPrompt = PromptGenerator.getFullPrompt("$name. $description".trim(), GenerationType.LOCATION)
    }

    CreatorFormLayout(
        accentColor = LocationColor,
        isSaving = isSaving,
        saveButtonText = if (existing != null) "Update Location" else "Create Location",
        saveEnabled = name.isNotBlank(),
        onSave = {
            isSaving = true
            scope.launch {
                val location = Location(
                    id = existing?.id ?: Random.nextLong().toString(),
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
        SectionHeader(Icons.Default.Map, "Identity", LocationColor)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Location Name *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        SectionHeader(Icons.Default.Image, "Media", LocationColor)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("https://...") }
            )
            OutlinedTextField(value = aiWidth, onValueChange = { aiWidth = it }, label = { Text("W") }, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(8.dp), singleLine = true)
            OutlinedTextField(value = aiHeight, onValueChange = { aiHeight = it }, label = { Text("H") }, modifier = Modifier.width(70.dp), shape = RoundedCornerShape(8.dp), singleLine = true)
            ImageGenerationButton(
                prompt = customPrompt.ifBlank { "Location: $name. $description" },
                onImageUrlGenerated = { imageUrl = it },
                accentColor = LocationColor,
                entityId = existing?.id,
                entityType = "location",
                width = aiWidth.toIntOrNull(),
                height = aiHeight.toIntOrNull()
            )
        }
        
        SectionHeader(Icons.Default.Description, "Details & Lore", LocationColor)
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("History, Inhabitants, Atmosphere...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 8,
            shape = RoundedCornerShape(12.dp)
        )

        SectionHeader(Icons.Default.AutoFixHigh, "AI Image Generation Prompt", LocationColor)
        OutlinedTextField(
            value = customPrompt,
            onValueChange = { customPrompt = it },
            label = { Text("AI Prompt") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Detailed description for AI generation...") }
        )
    }
}

@Composable
private fun CreatorSelection(onSelect: (CreatorType) -> Unit) {
    val types = listOf(
        CreatorType.Character,
        CreatorType.Item(),
        CreatorType.Monster(),
        CreatorType.Npc(),
        CreatorType.Location()
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (hovered) type.color.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
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
                shape = RoundedCornerShape(20.dp),
                color = type.color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = type.color
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                type.title, 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = type.color
            )
            Text(
                "New", 
                style = MaterialTheme.typography.labelMedium,
                color = type.color.copy(alpha = 0.6f)
            )
        }
    }
}
