package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.presentation.charactercreate.CharacterCreateScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.random.Random

sealed class CreatorType(val title: String, val icon: ImageVector) {
    data object Character : CreatorType("Character", Icons.Default.PersonAdd)
    data class Item(val existingItem: com.dnd.helper.domain.model.Item? = null, val ownerId: String? = null) : CreatorType("Item", Icons.Default.AddBox)
    data class Monster(val existingMonster: com.dnd.helper.domain.model.Monster? = null) : CreatorType("Monster", Icons.Default.BugReport)
    data class Npc(val existingNpc: com.dnd.helper.domain.model.Npc? = null) : CreatorType("NPC", Icons.Default.EmojiPeople)
    data class Location(val existingLocation: com.dnd.helper.domain.model.Location? = null) : CreatorType("Location", Icons.Default.AddLocation)
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    selectedType = null
                    onBack()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (isEditing) "Edit ${selectedType!!.title}" else "Create New ${selectedType!!.title}", 
                    style = MaterialTheme.typography.headlineSmall
                )
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
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("NPC Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = background,
            onValueChange = { background = it },
            label = { Text("Background / Role") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / Notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )
        
        Button(
            onClick = {
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
            },
            enabled = name.isNotBlank() && !isSaving,
            modifier = Modifier.align(Alignment.End)
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(if (existing != null) "Update NPC" else "Create NPC")
            }
        }
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
    
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Monster Name") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = cr, onValueChange = { cr = it }, label = { Text("CR") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = alignment, onValueChange = { alignment = it }, label = { Text("Alignment") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Size") }, modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value = hp, onValueChange = { hp = it }, label = { Text("HP") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = ac, onValueChange = { ac = it }, label = { Text("AC") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = speed, onValueChange = { speed = it }, label = { Text("Speed") }, modifier = Modifier.weight(1f))
        }

        Text("Abilities", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = str, onValueChange = { str = it }, label = { Text("STR") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = dex, onValueChange = { dex = it }, label = { Text("DEX") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = con, onValueChange = { con = it }, label = { Text("CON") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = int, onValueChange = { int = it }, label = { Text("INT") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = wis, onValueChange = { wis = it }, label = { Text("WIS") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = cha, onValueChange = { cha = it }, label = { Text("CHA") }, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / Attacks") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5
        )
        
        Button(
            onClick = {
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
            },
            enabled = name.isNotBlank() && !isSaving,
            modifier = Modifier.align(Alignment.End)
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(if (existing != null) "Update Monster" else "Create Monster")
            }
        }
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
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = repository.getCharacters()
        if (result is com.dnd.helper.domain.common.Result.Success) {
            characters = result.data.filter { it.id.isNotBlank() }.distinctBy { it.id.trim() }
            if (selectedCharId.isEmpty()) selectedCharId = characters.firstOrNull()?.id ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (existing == null) {
            Text("Assign to Character:", style = MaterialTheme.typography.titleMedium)
            if (characters.isEmpty()) {
                CircularProgressIndicator()
            } else {
                Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                    characters.forEach { char ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedCharId == char.id, onClick = { selectedCharId = char.id })
                            Text(char.name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        } else {
            Text("Editing item for character: ${characters.find { it.id == selectedCharId }?.name ?: selectedCharId}", 
                 style = MaterialTheme.typography.titleMedium)
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            var rarityExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(onClick = { rarityExpanded = true }, modifier = Modifier.fillMaxWidth()) {
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
                OutlinedButton(onClick = { slotExpanded = true }, modifier = Modifier.fillMaxWidth()) {
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

        OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        
        Button(
            onClick = {
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
            },
            enabled = name.isNotBlank() && selectedCharId.isNotBlank() && !isSaving,
            modifier = Modifier.align(Alignment.End)
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(if (existing != null) "Update Item" else "Create and Assign Item")
            }
        }
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
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Location Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth()
        )
        
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        
        Button(
            onClick = {
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
            },
            enabled = name.isNotBlank() && !isSaving,
            modifier = Modifier.align(Alignment.End)
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(if (existing != null) "Update Location" else "Create Location")
            }
        }
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("What do you want to create?", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(32.dp))
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
    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.size(160.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = type.icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(type.title, style = MaterialTheme.typography.titleLarge)
        }
    }
}
