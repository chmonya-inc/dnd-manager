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
    data object Item : CreatorType("Item", Icons.Default.AddBox)
    data object Monster : CreatorType("Monster", Icons.Default.BugReport)
    data object Npc : CreatorType("NPC", Icons.Default.EmojiPeople)
    data object Location : CreatorType("Location", Icons.Default.AddLocation)
}

@Composable
fun CreatorScreen(initialType: CreatorType? = null) {
    var selectedType by remember(initialType) { mutableStateOf<CreatorType?>(initialType) }

    if (selectedType == null) {
        CreatorSelection(onSelect = { selectedType = it })
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedType = null }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("Create New ${selectedType!!.title}", style = MaterialTheme.typography.headlineSmall)
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedType) {
                    CreatorType.Character -> CharacterCreateScreen(
                        onBackClick = { selectedType = null },
                        onCharacterCreated = { selectedType = null }
                    )
                    CreatorType.Location -> LocationCreateForm(
                        onBackClick = { selectedType = null },
                        onCreated = { selectedType = null }
                    )
                    CreatorType.Monster -> MonsterCreateForm(
                        onBackClick = { selectedType = null },
                        onCreated = { selectedType = null }
                    )
                    CreatorType.Item -> ItemCreateForm(
                        onBackClick = { selectedType = null },
                        onCreated = { selectedType = null }
                    )
                    CreatorType.Npc -> NpcCreateForm(
                        onBackClick = { selectedType = null },
                        onCreated = { selectedType = null }
                    )
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun NpcCreateForm(
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var background by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
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
                        id = "npc-${Random.nextLong()}",
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
                Text("Create NPC")
            }
        }
    }
}

@Composable
private fun MonsterCreateForm(
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var cr by remember { mutableStateOf("1") }
    var type by remember { mutableStateOf("Humanoid") }
    var alignment by remember { mutableStateOf("Neutral") }
    var size by remember { mutableStateOf("Medium") }
    var hp by remember { mutableStateOf("10") }
    var ac by remember { mutableStateOf("10") }
    var speed by remember { mutableStateOf("30") }
    
    var str by remember { mutableStateOf("10") }
    var dex by remember { mutableStateOf("10") }
    var con by remember { mutableStateOf("10") }
    var int by remember { mutableStateOf("10") }
    var wis by remember { mutableStateOf("10") }
    var cha by remember { mutableStateOf("10") }
    
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
                        id = "monster-${Random.nextLong()}",
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
                Text("Create Monster")
            }
        }
    }
}

@Composable
private fun ItemCreateForm(
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var characters by remember { mutableStateOf<List<com.dnd.helper.domain.model.Character>>(emptyList()) }
    var selectedCharId by remember { mutableStateOf("") }
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var rarity by remember { mutableStateOf(ItemRarity.COMMON) }
    var slot by remember { mutableStateOf<EquipmentSlot?>(EquipmentSlot.MAIN_HAND) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = repository.getCharacters()
        if (result is com.dnd.helper.domain.common.Result.Success) {
            // Fix: Trim and filter IDs to prevent duplicates in the selection list
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
        Text("Assign to Character:", style = MaterialTheme.typography.titleMedium)
        if (characters.isEmpty()) {
            CircularProgressIndicator()
        } else {
            characters.forEach { char ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedCharId == char.id, onClick = { selectedCharId = char.id })
                    Text(char.name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Simple Rarity Picker
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
            
            // Simple Slot Picker
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
                        id = "item-${Random.nextLong()}",
                        name = name,
                        description = description,
                        imageUrl = imageUrl.ifBlank { null },
                        rarity = rarity,
                        slot = slot
                    )
                    val char = characters.find { it.id == selectedCharId }
                    if (char != null) {
                        repository.saveCharacter(char.copy(items = char.items + item))
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
                Text("Create and Assign Item")
            }
        }
    }
}

@Composable
private fun LocationCreateForm(
    onBackClick: () -> Unit,
    onCreated: () -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
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
                        id = Random.nextLong().toString(),
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
                Text("Create Location")
            }
        }
    }
}

@Composable
private fun CreatorSelection(onSelect: (CreatorType) -> Unit) {
    val types = listOf(
        CreatorType.Character,
        CreatorType.Item,
        CreatorType.Monster,
        CreatorType.Location
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
