package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.*
import com.dnd.helper.presentation.utils.itemToIcon
import com.dnd.helper.presentation.utils.toColor
import org.koin.compose.viewmodel.koinViewModel

// Consistent colors with CreatorScreen
private val MonsterColor = Color(0xFFEF5350)
private val NpcColor = Color(0xFF66BB6A)
private val LocationColor = Color(0xFF42A5F5)
private val ItemColor = Color(0xFFFFA726)

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = koinViewModel(),
    presentationViewModel: PresentationViewModel = koinViewModel(),
    onNavigateToCreator: (CreatorType) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    val currentThemeColor = when(state.selectedType) {
        LibraryType.Items -> ItemColor
        LibraryType.Mobs -> MonsterColor
        LibraryType.Npcs -> NpcColor
        LibraryType.Locations -> LocationColor
        LibraryType.Templates -> ItemColor
    }

    // Drag and Drop State
    var draggedItemInfo by remember { mutableStateOf<Triple<Item, String, String>?>(null) } // Item, fromId, fromName
    var mousePosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var dropTargetId by remember { mutableStateOf<String?>(null) }
    val characterBounds = remember { mutableStateMapOf<String, androidx.compose.ui.layout.LayoutCoordinates>() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(tonalElevation = 2.dp, shadowElevation = 2.dp) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    TabRow(
                        selectedTabIndex = when(state.selectedType) {
                            LibraryType.Items -> 0
                            LibraryType.Mobs -> 1
                            LibraryType.Npcs -> 2
                            LibraryType.Locations -> 3
                            LibraryType.Templates -> 4
                        },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = currentThemeColor,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[when(state.selectedType) {
                                    LibraryType.Items -> 0
                                    LibraryType.Mobs -> 1
                                    LibraryType.Npcs -> 2
                                    LibraryType.Locations -> 3
                                    LibraryType.Templates -> 4
                                }]),
                                color = currentThemeColor
                            )
                        },
                        modifier = Modifier.padding(end = 64.dp)
                    ) {
                        val tabs = listOf(
                            Triple(LibraryType.Items, ItemColor, Icons.Default.ShoppingBag),
                            Triple(LibraryType.Mobs, MonsterColor, Icons.Default.BugReport),
                            Triple(LibraryType.Npcs, NpcColor, Icons.Default.EmojiPeople),
                            Triple(LibraryType.Locations, LocationColor, Icons.Default.Explore),
                            Triple(LibraryType.Templates, ItemColor, Icons.Default.AutoAwesome)
                        )
                        
                        tabs.forEach { (type, color, icon) ->
                            val selected = state.selectedType == type
                            Tab(
                                selected = selected,
                                onClick = { viewModel.onTypeSelected(type) },
                                text = { 
                                    Text(
                                        type.title, 
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                    ) 
                                },
                                icon = { 
                                Icon(
                                    imageVector = icon, 
                                    contentDescription = null,
                                    tint = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                                }
                            )
                        }
                    }
                    
                    Row(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp)) {
                        IconButton(onClick = { viewModel.refreshData(force = true) }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = currentThemeColor)
                } else {
                    when (state.selectedType) {
                        LibraryType.Items -> ItemLibraryGrid(
                            characters = state.characters, 
                            presentationViewModel = presentationViewModel, 
                            onDeleteItem = viewModel::deleteItem,
                            onAddItem = viewModel::addItem,
                            onEdit = { item, ownerId -> onNavigateToCreator(CreatorType.Item(item, ownerId)) },
                            dropTargetId = dropTargetId,
                            characterBounds = characterBounds,
                            onDragStart = { info -> draggedItemInfo = info },
                            onDrag = { pos -> 
                                mousePosition = pos
                                // Check for drop target
                                dropTargetId = characterBounds.entries.find { (_, coords) ->
                                    if (coords.isAttached) {
                                        coords.boundsInWindow().contains(mousePosition)
                                    } else false
                                }?.key
                            },
                            onDragEnd = {
                                val itemToMove = draggedItemInfo
                                val targetId = dropTargetId
                                if (itemToMove != null && targetId != null && itemToMove.second != targetId) {
                                    viewModel.moveItemBetweenCharacters(itemToMove.first, itemToMove.second, targetId)
                                }
                                draggedItemInfo = null
                                dropTargetId = null
                            },
                            onDragCancel = {
                                draggedItemInfo = null
                                dropTargetId = null
                            }
                        )
                        LibraryType.Mobs -> MonsterGrid(
                            monsters = state.monsters, 
                            presentationViewModel = presentationViewModel, 
                            onDelete = viewModel::deleteMonster,
                            onCreateNew = { onNavigateToCreator(CreatorType.Monster()) },
                            onEdit = { monster -> onNavigateToCreator(CreatorType.Monster(monster)) }
                        )
                        LibraryType.Npcs -> NpcGrid(
                            npcs = state.npcs, 
                            presentationViewModel = presentationViewModel, 
                            onDelete = viewModel::deleteNpc,
                            onCreateNew = { onNavigateToCreator(CreatorType.Npc()) },
                            onEdit = { npc -> onNavigateToCreator(CreatorType.Npc(npc)) }
                        )
                        LibraryType.Locations -> LocationGrid(
                            locations = state.locations, 
                            presentationViewModel = presentationViewModel, 
                            onDelete = viewModel::deleteLocation,
                            onCreateNew = { onNavigateToCreator(CreatorType.Location()) },
                            onEdit = { location -> onNavigateToCreator(CreatorType.Location(location)) }
                        )
                        LibraryType.Templates -> TemplateLibraryGrid(
                            characters = state.characters,
                            onAddItem = viewModel::addItem
                        )
                    }
                }
            }
        }

        // Ghost Overlay
        draggedItemInfo?.let { (item, _, _) ->
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            mousePosition.x.toInt() - 50,
                            mousePosition.y.toInt() - 20
                        )
                    }
                    .size(width = 200.dp, height = 40.dp)
                    .background(ItemColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(item.name, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    title: String,
    color: Color,
    icon: ImageVector,
    onAdd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        }
        
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = color),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("New ${title.removeSuffix("s")}")
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
        CategoryHeader("NPCs", NpcColor, Icons.Default.EmojiPeople, onCreateNew)
        
        if (npcs.isEmpty()) {
            EmptyLibraryState("No NPCs found", NpcColor)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
private fun EmptyLibraryState(message: String, color: Color) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = color.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun LibraryCardActions(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPresent: () -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(18.dp))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
        }
        FilledIconButton(
            onClick = onPresent,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = accentColor)
        ) {
            Icon(Icons.Default.Tv, "Present", modifier = Modifier.size(16.dp))
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val imageUrl = npc.displayImageUrl
                val isGenerating = imageUrl?.startsWith("generating:") == true

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = if (isGenerating) null else imageUrl,
                        contentDescription = npc.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isGenerating) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(NpcColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.EmojiPeople, null, modifier = Modifier.size(64.dp), tint = NpcColor.copy(alpha = 0.3f))
                    }
                }
                
                // Content overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                )
                
                Text(
                    npc.name, 
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                if (npc.background.isNotBlank()) {
                    Surface(color = NpcColor.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            npc.background, 
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium, 
                            color = NpcColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                LibraryCardActions(onEdit, onDelete, onPresent, NpcColor)
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
        CategoryHeader("Monsters", MonsterColor, Icons.Default.BugReport, onCreateNew)
        
        if (monsters.isEmpty()) {
            EmptyLibraryState("No monsters found", MonsterColor)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val imageUrl = monster.displayImageUrl
                val isGenerating = imageUrl?.startsWith("generating:") == true

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = if (isGenerating) null else imageUrl,
                        contentDescription = monster.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isGenerating) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(MonsterColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.BugReport, null, modifier = Modifier.size(64.dp), tint = MonsterColor.copy(alpha = 0.3f))
                    }
                }
                
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "CR ${monster.challengeRating}", 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                )

                Text(
                    monster.name, 
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "${monster.size} ${monster.type} · ${monster.alignment}", 
                    style = MaterialTheme.typography.bodySmall, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(Modifier.height(8.dp))
                LibraryCardActions(onEdit, onDelete, onPresent, MonsterColor)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemLibraryGrid(
    characters: List<com.dnd.helper.domain.model.Character>,
    presentationViewModel: PresentationViewModel,
    onDeleteItem: (String, String) -> Unit,
    onAddItem: (String, Item) -> Unit,
    onEdit: (Item, String) -> Unit,
    dropTargetId: String?,
    characterBounds: MutableMap<String, androidx.compose.ui.layout.LayoutCoordinates>,
    onDragStart: (Triple<Item, String, String>) -> Unit,
    onDrag: (androidx.compose.ui.geometry.Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    
    val distinctCharacters = remember(characters) { 
        characters.filter { it.id.isNotBlank() }.distinctBy { it.id.trim() } 
    }

    if (showCreateDialog) {
        AddItemToCharacterDialog(
            characters = distinctCharacters,
            onDismiss = { showCreateDialog = false },
            onSave = onAddItem
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CategoryHeader("Items Library", ItemColor, Icons.Default.ShoppingBag, { showCreateDialog = true })
        
        if (distinctCharacters.isEmpty()) {
            EmptyLibraryState("No characters found to assign items", ItemColor)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(distinctCharacters) { character ->
                    Column {
                        val isHighlighted = dropTargetId == character.id
                        Surface(
                            color = if (isHighlighted) ItemColor.copy(alpha = 0.2f) else ItemColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isHighlighted) BorderStroke(2.dp, ItemColor) else null,
                            modifier = Modifier.onGloballyPositioned { coords ->
                                characterBounds[character.id] = coords
                            }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = ItemColor)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = character.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ItemColor
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        if (character.items.isEmpty()) {
                            Text(
                                "Inventory is empty", 
                                style = MaterialTheme.typography.bodySmall, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.padding(start = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                maxItemsInEachRow = Int.MAX_VALUE
                            ) {
                                character.items.forEach { item ->
                                    var itemCoords: androidx.compose.ui.layout.LayoutCoordinates? by remember { mutableStateOf(null) }
                                    Box(modifier = Modifier.width(240.dp)
                                        .onGloballyPositioned { itemCoords = it }
                                        .pointerInput(item.id) {
                                            detectDragGestures(
                                                onDragStart = { _ ->
                                                    onDragStart(Triple(item, character.id, character.name))
                                                },
                                                onDrag = { change, _ ->
                                                    val base = itemCoords?.boundsInWindow()?.topLeft ?: androidx.compose.ui.geometry.Offset.Zero
                                                    onDrag(base + change.position)
                                                },
                                                onDragEnd = onDragEnd,
                                                onDragCancel = onDragCancel
                                            )
                                        }
                                    ) {
                                        ItemLibraryCard(
                                            item = item,
                                            onPresent = { presentationViewModel.addItem(item.name, type = "Item", imageUrl = item.imageUrl) },
                                            onDelete = { onDeleteItem(character.id, item.id) },
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
    var rarity by remember { mutableStateOf(ItemRarity.COMMON) }
    var slot by remember { mutableStateOf<EquipmentSlot?>(EquipmentSlot.MAIN_HAND) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Item", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Receiver:", style = MaterialTheme.typography.labelLarge)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.heightIn(max = 140.dp).verticalScroll(rememberScrollState()).padding(4.dp)) {
                        characters.forEach { char ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { selectedCharacterId = char.id },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCharacterId == char.id, 
                                    onClick = { selectedCharacterId = char.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = ItemColor)
                                )
                                Text(char.name, fontWeight = if (selectedCharacterId == char.id) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = itemName, 
                    onValueChange = { itemName = it }, 
                    label = { Text("Item Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var rarityExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { rarityExpanded = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = rarity.toColor())
                        ) {
                            Text(rarity.name, maxLines = 1)
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
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(slot?.name ?: "None", maxLines = 1)
                        }
                        DropdownMenu(expanded = slotExpanded, onDismissRequest = { slotExpanded = false }) {
                            DropdownMenuItem(text = { Text("None") }, onClick = { slot = null; slotExpanded = false })
                            EquipmentSlot.entries.forEach { s ->
                                DropdownMenuItem(text = { Text(s.name) }, onClick = { slot = s; slotExpanded = false })
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = itemDescription, 
                    onValueChange = { itemDescription = it }, 
                    label = { Text("Description") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = itemImageUrl, 
                    onValueChange = { itemImageUrl = it }, 
                    label = { Text("Image URL") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newItem = Item(
                        id = "item-${kotlin.random.Random.nextInt()}",
                        name = itemName,
                        slot = slot,
                        rarity = rarity,
                        description = itemDescription,
                        imageUrl = itemImageUrl.ifBlank { null }
                    )
                    onSave(selectedCharacterId, newItem)
                    onDismiss()
                },
                enabled = itemName.isNotBlank() && selectedCharacterId.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = ItemColor),
                shape = RoundedCornerShape(10.dp)
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
    val rarityColor = item.rarity.toColor()
    val icon = itemToIcon(item)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.5f))
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                val imageUrl = item.displayImageUrl
                val isGenerating = imageUrl?.startsWith("generating:") == true

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = if (isGenerating) null else imageUrl,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isGenerating) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(rarityColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = rarityColor.copy(alpha = 0.4f)
                        )
                    }
                }

                // Content overlay gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))))
                )

                Text(
                    item.name,
                    modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                // Rarity Tag
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    color = rarityColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        item.rarity.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                if (item.description.isNotBlank()) {
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(8.dp))
                LibraryCardActions(onEdit, onDelete, onPresent, rarityColor)
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
        CategoryHeader("Locations", LocationColor, Icons.Default.Explore, onCreateNew)
        
        if (locations.isEmpty()) {
            EmptyLibraryState("No locations found", LocationColor)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val imageUrl = location.displayImageUrl
                val isGenerating = imageUrl?.startsWith("generating:") == true

                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = if (isGenerating) null else imageUrl,
                        contentDescription = location.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    if (isGenerating) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(LocationColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Map, null, modifier = Modifier.size(64.dp), tint = LocationColor.copy(alpha = 0.3f))
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                )
                
                Text(
                    location.name, 
                    modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                if (location.description.isNotBlank()) {
                    Text(
                        location.description, 
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(12.dp))
                LibraryCardActions(onEdit, onDelete, onPresent, LocationColor)
            }
        }
    }
}

@Composable
private fun TemplateLibraryGrid(
    characters: List<com.dnd.helper.domain.model.Character>,
    onAddItem: (String, Item) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf<Item?>(null) }
    
    val distinctCharacters = remember(characters) { 
        characters.filter { it.id.isNotBlank() }.distinctBy { it.id.trim() } 
    }

    if (selectedTemplate != null) {
        AssignTemplateDialog(
            template = selectedTemplate!!,
            characters = distinctCharacters,
            onDismiss = { selectedTemplate = null },
            onConfirm = { charId, item ->
                onAddItem(charId, item)
                selectedTemplate = null
            }
        )
    }

    Column {
        CategoryHeader("Generic Templates", ItemColor, Icons.Default.AutoAwesome, {})
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 240.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(ItemTemplates.genericItems) { template ->
                TemplateCard(template, onClick = { selectedTemplate = template })
            }
        }
    }
}

@Composable
private fun TemplateCard(template: Item, onClick: () -> Unit) {
    val rarityColor = template.rarity.toColor()
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, rarityColor.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp),
                color = rarityColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(itemToIcon(template), null, tint = rarityColor)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(template.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(template.rarity.name, style = MaterialTheme.typography.labelSmall, color = rarityColor)
            }
        }
    }
}

@Composable
private fun AssignTemplateDialog(
    template: Item,
    characters: List<com.dnd.helper.domain.model.Character>,
    onDismiss: () -> Unit,
    onConfirm: (String, Item) -> Unit
) {
    var selectedCharacterId by remember { mutableStateOf(characters.firstOrNull()?.id ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assign ${template.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Select Character:")
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(characters) { char ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selectedCharacterId = char.id }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedCharacterId == char.id, onClick = { selectedCharacterId = char.id })
                            Text(char.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(selectedCharacterId, template.copy(id = "item-${kotlin.random.Random.nextInt()}"))
                },
                enabled = selectedCharacterId.isNotBlank()
            ) {
                Text("Give to Character")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
