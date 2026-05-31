package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

sealed class LibraryType(val title: String, val icon: ImageVector) {
    data object Items : LibraryType("Items", Icons.Default.ShoppingBag)
    data object Mobs : LibraryType("Monsters", Icons.Default.BugReport) 
    data object Npcs : LibraryType("NPCs", Icons.Default.EmojiPeople)
    data object Locations : LibraryType("Locations", Icons.Default.Map)
}

@Composable
fun LibraryScreen(
    presentationViewModel: PresentationViewModel = koinViewModel(),
    onNavigateToCreator: (CreatorType) -> Unit = {}
) {
    var selectedType by remember { mutableStateOf<LibraryType>(LibraryType.Items) }
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var monsters by remember { mutableStateOf<List<Monster>>(emptyList()) }
    var npcs by remember { mutableStateOf<List<Npc>>(emptyList()) }
    var characters by remember { mutableStateOf<List<com.dnd.helper.domain.model.Character>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    fun refreshData(force: Boolean = false) {
        scope.launch {
            isLoading = true
            when (selectedType) {
                LibraryType.Locations -> {
                    val result = repository.getLocations(forceRefresh = force)
                    if (result is com.dnd.helper.domain.common.Result.Success) locations = result.data
                }
                LibraryType.Mobs -> {
                    val result = repository.getMonsters(forceRefresh = force)
                    if (result is com.dnd.helper.domain.common.Result.Success) monsters = result.data
                }
                LibraryType.Npcs -> {
                    val result = repository.getNpcs(forceRefresh = force)
                    if (result is com.dnd.helper.domain.common.Result.Success) npcs = result.data
                }
                LibraryType.Items -> {
                    val result = repository.getCharacters(forceRefresh = force)
                    if (result is com.dnd.helper.domain.common.Result.Success) characters = result.data
                }
            }
            isLoading = false
        }
    }

    LaunchedEffect(selectedType) {
        refreshData()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            TabRow(
                selectedTabIndex = when(selectedType) {
                    LibraryType.Items -> 0
                    LibraryType.Mobs -> 1
                    LibraryType.Npcs -> 2
                    LibraryType.Locations -> 3
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(end = 48.dp)
            ) {
                Tab(
                    selected = selectedType == LibraryType.Items,
                    onClick = { selectedType = LibraryType.Items },
                    text = { Text(LibraryType.Items.title) },
                    icon = { Icon(LibraryType.Items.icon, null) }
                )
                Tab(
                    selected = selectedType == LibraryType.Mobs,
                    onClick = { selectedType = LibraryType.Mobs },
                    text = { Text(LibraryType.Mobs.title) },
                    icon = { Icon(LibraryType.Mobs.icon, null) }
                )
                Tab(
                    selected = selectedType == LibraryType.Npcs,
                    onClick = { selectedType = LibraryType.Npcs },
                    text = { Text(LibraryType.Npcs.title) },
                    icon = { Icon(LibraryType.Npcs.icon, null) }
                )
                Tab(
                    selected = selectedType == LibraryType.Locations,
                    onClick = { selectedType = LibraryType.Locations },
                    text = { Text(LibraryType.Locations.title) },
                    icon = { Icon(LibraryType.Locations.icon, null) }
                )
            }
            
            IconButton(
                onClick = { refreshData(force = true) },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (selectedType) {
                    LibraryType.Items -> ItemLibraryGrid(
                        characters = characters, 
                        presentationViewModel = presentationViewModel, 
                        onUpdate = { refreshData(force = true) },
                        onEdit = { item, ownerId -> onNavigateToCreator(CreatorType.Item(item, ownerId)) }
                    )
                    LibraryType.Mobs -> MonsterGrid(
                        monsters = monsters, 
                        presentationViewModel = presentationViewModel, 
                        onDelete = { id ->
                            scope.launch {
                                repository.deleteMonster(id)
                                monsters = monsters.filter { it.id != id }
                            }
                        },
                        onCreateNew = { onNavigateToCreator(CreatorType.Monster()) },
                        onEdit = { monster -> onNavigateToCreator(CreatorType.Monster(monster)) }
                    )
                    LibraryType.Npcs -> NpcGrid(
                        npcs = npcs, 
                        presentationViewModel = presentationViewModel, 
                        onDelete = { id ->
                            scope.launch {
                                repository.deleteNpc(id)
                                npcs = npcs.filter { it.id != id }
                            }
                        },
                        onCreateNew = { onNavigateToCreator(CreatorType.Npc()) },
                        onEdit = { npc -> onNavigateToCreator(CreatorType.Npc(npc)) }
                    )
                    LibraryType.Locations -> LocationGrid(
                        locations = locations, 
                        presentationViewModel = presentationViewModel, 
                        onDelete = { id ->
                            scope.launch {
                                repository.deleteLocation(id)
                                locations = locations.filter { it.id != id }
                            }
                        },
                        onCreateNew = { onNavigateToCreator(CreatorType.Location()) },
                        onEdit = { location -> onNavigateToCreator(CreatorType.Location(location)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NpcGrid(
    npcs: List<Npc>,
    presentationViewModel: PresentationViewModel,
    onDelete: (String) -> Unit,
    onCreateNew: () -> Unit,
    onEdit: (Npc) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("NPCs", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onCreateNew) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New NPC")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (npcs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No NPCs found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(npcs, key = { it.id }) { npc ->
                    NpcLibraryCard(
                        npc = npc,
                        onPresent = { presentationViewModel.addItem(npc.name, type = "NPC", imageUrl = npc.displayImageUrl) },
                        onDelete = { onDelete(npc.id) },
                        onEdit = { onEdit(npc) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NpcLibraryCard(
    npc: Npc,
    onPresent: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.Black)) {
                if (npc.displayImageUrl?.isNotBlank() ?: false) {
                    AsyncImage(
                        model = npc.displayImageUrl,
                        contentDescription = npc.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.EmojiPeople,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).align(Alignment.Center),
                        tint = Color.DarkGray
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(npc.name, style = MaterialTheme.typography.titleMedium)
                if (npc.background.isNotBlank()) {
                    Text(npc.background, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
                
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onPresent) {
                        Icon(Icons.Default.Tv, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonsterGrid(
    monsters: List<Monster>,
    presentationViewModel: PresentationViewModel,
    onDelete: (String) -> Unit,
    onCreateNew: () -> Unit,
    onEdit: (Monster) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Monsters", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onCreateNew) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Monster")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (monsters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No monsters found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 200.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(monsters, key = { it.id }) { monster ->
                    MonsterLibraryCard(
                        monster = monster,
                        onPresent = { presentationViewModel.addItem(monster.name, type = "Monster", imageUrl = monster.displayImageUrl) },
                        onDelete = { onDelete(monster.id) },
                        onEdit = { onEdit(monster) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonsterLibraryCard(
    monster: Monster,
    onPresent: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.Black)) {
                if (monster.displayImageUrl?.isNotBlank() ?: false) {
                    AsyncImage(
                        model = monster.displayImageUrl,
                        contentDescription = monster.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).align(Alignment.Center),
                        tint = Color.DarkGray
                    )
                }
                
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        "CR ${monster.challengeRating}", 
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(monster.name, style = MaterialTheme.typography.titleMedium)
                Text("${monster.size} ${monster.type}, ${monster.alignment}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onPresent) {
                        Icon(Icons.Default.Tv, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemLibraryGrid(
    characters: List<com.dnd.helper.domain.model.Character>,
    presentationViewModel: PresentationViewModel,
    onUpdate: () -> Unit,
    onEdit: (Item, String) -> Unit
) {
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }
    
    // Ensure characters list is unique for the picker and has valid IDs
    val distinctCharacters = remember(characters) { 
        characters.filter { it.id.isNotBlank() }.distinctBy { it.id.trim() } 
    }

    if (showCreateDialog) {
        AddItemToCharacterDialog(
            characters = distinctCharacters,
            onDismiss = { showCreateDialog = false },
            onSave = { characterId, item ->
                scope.launch {
                    val char = characters.find { it.id == characterId }
                    if (char != null) {
                        val updatedChar = char.copy(items = char.items + item)
                        repository.saveCharacter(updatedChar)
                        onUpdate()
                    }
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Items Library", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Item")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (distinctCharacters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No characters found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(distinctCharacters) { character ->
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Character: ${character.name}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        if (character.items.isEmpty()) {
                            Text(
                                "No items in inventory", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 28.dp)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.padding(start = 28.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                maxItemsInEachRow = Int.MAX_VALUE
                            ) {
                                character.items.forEach { item ->
                                    Box(modifier = Modifier.width(220.dp)) {
                                        ItemLibraryCard(
                                            item = item,
                                            onPresent = { presentationViewModel.addItem(item.name, type = "Item", imageUrl = item.imageUrl) },
                                            onDelete = {
                                                scope.launch {
                                                    val updatedChar = character.copy(items = character.items.filter { it.id != item.id })
                                                    repository.saveCharacter(updatedChar)
                                                    onUpdate()
                                                }
                                            },
                                            onEdit = { onEdit(item, character.id) }
                                        )
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

@Composable
fun AddItemToCharacterDialog(
    characters: List<com.dnd.helper.domain.model.Character>,
    onDismiss: () -> Unit,
    onSave: (String, Item) -> Unit
) {
    var selectedCharacterId by remember { mutableStateOf(characters.firstOrNull()?.id ?: "") }
    var itemName by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var itemImageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Assign to Character:")
                // Scrollable character picker
                Column(modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())) {
                    characters.forEach { char ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selectedCharacterId = char.id },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedCharacterId == char.id, onClick = { selectedCharacterId = char.id })
                            Text(char.name)
                        }
                    }
                }
                
                OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name") })
                OutlinedTextField(value = itemDescription, onValueChange = { itemDescription = it }, label = { Text("Description") })
                OutlinedTextField(value = itemImageUrl, onValueChange = { itemImageUrl = it }, label = { Text("Image URL") })
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newItem = Item(
                        id = "item-${kotlin.random.Random.nextInt()}",
                        name = itemName,
                        slot = EquipmentSlot.MAIN_HAND,
                        rarity = ItemRarity.COMMON,
                        description = itemDescription,
                        imageUrl = itemImageUrl.ifBlank { null }
                    )
                    onSave(selectedCharacterId, newItem)
                    onDismiss()
                },
                enabled = itemName.isNotBlank() && selectedCharacterId.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ItemLibraryCard(
    item: Item,
    onPresent: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
            
            if (item.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(item.description, style = MaterialTheme.typography.bodySmall, maxLines = 2, fontSize = 11.sp, lineHeight = 14.sp)
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onPresent, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Tv, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun LocationGrid(
    locations: List<Location>,
    presentationViewModel: PresentationViewModel,
    onDelete: (String) -> Unit,
    onCreateNew: () -> Unit,
    onEdit: (Location) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Locations", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = onCreateNew) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New Location")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (locations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No locations found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 240.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(locations, key = { it.id }) { location ->
                    LocationLibraryCard(
                        location = location,
                        onPresent = { presentationViewModel.addItem(location.name, type = "Location", imageUrl = location.displayImageUrl, isBackground = true) },
                        onDelete = { onDelete(location.id) },
                        onEdit = { onEdit(location) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationLibraryCard(
    location: Location,
    onPresent: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Color.Black)) {
                if (location.displayImageUrl?.isNotBlank() ?: false) {
                    AsyncImage(
                        model = location.displayImageUrl,
                        contentDescription = location.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).align(Alignment.Center),
                        tint = Color.DarkGray
                    )
                }
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(location.name, style = MaterialTheme.typography.titleMedium)
                if (location.description.isNotBlank()) {
                    Text(
                        location.description, 
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = onPresent) {
                        Icon(Icons.Default.Tv, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
