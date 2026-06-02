package com.dnd.helper.presentation.desktop

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.*
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.presentation.characterlist.CharacterListViewModel
import com.dnd.helper.presentation.utils.itemToIcon
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.sqrt

import com.dnd.helper.theme.LocalDndColors

// Theme colors
private val MonsterColor: Color @Composable get() = LocalDndColors.current.monster
private val NpcColor: Color @Composable get() = LocalDndColors.current.npc
private val LocationColor: Color @Composable get() = LocalDndColors.current.location
private val BattlefieldColor: Color @Composable get() = LocalDndColors.current.battlefield
private val ItemColor: Color @Composable get() = LocalDndColors.current.item
private val CharacterColor: Color @Composable get() = LocalDndColors.current.character

private const val LOGICAL_CANVAS_SIZE = 1000f
private const val PROXIMITY_THRESHOLD = 250f // Distance to trigger "fighting" state (increased for visible gap)

@Composable
fun PresentationScreen(
    viewModel: PresentationViewModel = koinViewModel(),
    characterListViewModel: CharacterListViewModel = koinViewModel()
) {
    val isWindowOpen by viewModel.isWindowOpen.collectAsState()
    val showStats by viewModel.showStats.collectAsState()
    val activeItems = viewModel.activeItems
    val characterListState by characterListViewModel.state.collectAsState()

    val monsters by viewModel.monsters.collectAsState()
    val npcs by viewModel.npcs.collectAsState()
    val locations by viewModel.locations.collectAsState()
    val battlefields by viewModel.battlefields.collectAsState()
    val events by viewModel.events.collectAsState()
    val activeEvent by viewModel.activeEvent.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSaveAsDialog by remember { mutableStateOf(false) }

    // Drag and Drop State
    var draggedItemInfo by remember { mutableStateOf<Triple<Item, String, String>?>(null) } // Item, fromId, fromName
    var mousePosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    var dropTargetId by remember { mutableStateOf<String?>(null) }
    val characterBounds = remember { mutableStateMapOf<String, androidx.compose.ui.layout.LayoutCoordinates>() }

    LaunchedEffect(Unit) {
        viewModel.refreshAll()
    }

    ExternalWindow(
        isOpen = isWindowOpen,
        onCloseRequest = { viewModel.setWindowOpen(false) }
    ) {
        com.dnd.helper.theme.DndHelperTheme {
            PlayerViewContent(activeItems = activeItems, showStats = showStats)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = if (isWindowOpen) Color(0xFF4CAF50).copy(alpha = 0.1f) else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isWindowOpen) Icons.Default.Tv else Icons.Default.TvOff, 
                                    contentDescription = null,
                                    tint = if (isWindowOpen) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Presentation Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                            Text(
                                text = if (isWindowOpen) "Live Feed Active" else "Display Offline",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isWindowOpen) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { viewModel.toggleStats() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showStats) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (showStats) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(if (showStats) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (showStats) "Projection Stats: ON" else "Projection Stats: OFF")
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Button(
                            onClick = { viewModel.toggleWindow() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isWindowOpen) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(if (isWindowOpen) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isWindowOpen) "Stop Projection" else "Start Projection")
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Workspace
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = activeEvent?.name ?: "Unsaved Scene",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (activeEvent != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            
                            if (activeEvent != null) {
                                // 1. Quick Save (Update Content)
                                IconButton(
                                    onClick = { viewModel.saveCurrentEvent(activeEvent!!.name) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Save, "Update Scene", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                
                                // 2. Save As (New Copy)
                                IconButton(
                                    onClick = { showSaveAsDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, "Save as New Scene", modifier = Modifier.size(18.dp))
                                }
                            } else {
                                // Save New Scene
                                IconButton(
                                    onClick = { showSaveAsDialog = true },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.AddCircle, "Save New Scene", modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            
                            if (showSaveAsDialog) {
                                SaveEventDialog(
                                    initialName = if (activeEvent != null) "${activeEvent!!.name} (Copy)" else "",
                                    onDismiss = { showSaveAsDialog = false },
                                    onSave = { name ->
                                        if (activeEvent != null) {
                                            viewModel.saveAsNewEvent(name)
                                        } else {
                                            viewModel.saveCurrentEvent(name)
                                        }
                                        showSaveAsDialog = false
                                    }
                                )
                            }
                        }
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                            Text("1000x1000", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    
                    WorkspaceContainer(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(MaterialTheme.shapes.medium)
                            .background(Color(0xFF0A0A0A))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
                        activeItems = activeItems,
                        onItemPositionChange = viewModel::updatePosition,
                        onItemSizeChange = viewModel::updateSize,
                        onItemRemove = viewModel::removeItem,
                        onItemHpChange = viewModel::updateHp,
                        onItemZoomChange = viewModel::updateZoom,
                        onItemOffsetChange = viewModel::updateOffset,
                        showStats = true, // DM always sees stats in workspace
                        isDM = true
                    )
                }
                
                // Sidebar
                Column(modifier = Modifier.width(320.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Assets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row {
                                    IconButton(onClick = { viewModel.refreshAll(force = true) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Libraries", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                            
                            Spacer(Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search assets...", style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                shape = MaterialTheme.shapes.small,
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                singleLine = true
                            )
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { viewModel.createNewScene() },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("New Scene", style = MaterialTheme.typography.labelLarge)
                                }
                                
                                OutlinedButton(
                                    onClick = { viewModel.clearItems() },
                                    modifier = Modifier.weight(1f),
                                    enabled = activeItems.isNotEmpty(),
                                    shape = MaterialTheme.shapes.small,
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clear All", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))

                    val distinctChars = characterListState.characters.distinctBy { it.id }
                        .filter { it.name.contains(searchQuery, ignoreCase = true) }
                    
                    // 1. Characters Section
                    FoldableSection(title = "Characters", count = distinctChars.size, icon = Icons.Default.Groups, color = CharacterColor) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(distinctChars) { character ->
                                PresenterSidebarRow(
                                    title = character.name, 
                                    accentColor = CharacterColor, 
                                    icon = Icons.Default.Person, 
                                    onClick = {
                                        viewModel.addItem(
                                            title = character.name, 
                                            type = "Character", 
                                            imageUrl = character.displayImageUrl,
                                            currentHp = character.currentHp,
                                            maxHp = character.maxHp,
                                            armorClass = character.combat.armorClass,
                                            stats = character.stats,
                                            sourceId = character.id,
                                            description = character.description
                                        )
                                    },
                                    modifier = Modifier.onGloballyPositioned { coords ->
                                        characterBounds[character.id] = coords
                                    },
                                    isHighlighted = dropTargetId == character.id
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 2. Monsters Section
                    val filteredMonsters = monsters.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    FoldableSection(title = "Monsters", count = filteredMonsters.size, icon = Icons.Default.BugReport, color = MonsterColor) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(filteredMonsters) { monster ->
                                PresenterSidebarRow(monster.name, MonsterColor, icon = Icons.Default.BugReport, onClick = {
                                    viewModel.addItem(
                                        title = monster.name, 
                                        type = "Monster", 
                                        imageUrl = monster.displayImageUrl,
                                        currentHp = monster.currentHp,
                                        maxHp = monster.maxHp,
                                        armorClass = monster.armorClass,
                                        stats = monster.stats,
                                        subInfo = "${monster.size} ${monster.type} · CR ${monster.challengeRating}",
                                        sourceId = monster.id,
                                        description = monster.description
                                    )
                                })
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 3. NPCs Section
                    val filteredNpcs = npcs.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    FoldableSection(title = "NPCs", count = filteredNpcs.size, icon = Icons.Default.EmojiPeople, color = NpcColor) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(filteredNpcs) { npc ->
                                PresenterSidebarRow(npc.name, NpcColor, icon = Icons.Default.EmojiPeople, onClick = {
                                    viewModel.addItem(
                                        title = npc.name, 
                                        type = "NPC", 
                                        imageUrl = npc.displayImageUrl,
                                        subInfo = npc.background,
                                        sourceId = npc.id,
                                        description = npc.description
                                    )
                                })
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 4. Locations Section
                    val filteredLocations = locations.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    FoldableSection(title = "Locations", count = filteredLocations.size, icon = Icons.Default.Explore, color = LocationColor) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(filteredLocations) { location ->
                                PresenterSidebarRow(location.name, LocationColor, icon = Icons.Default.Explore, onClick = {
                                    viewModel.addItem(location.name, "Location", imageUrl = location.displayImageUrl, isBackground = true)
                                })
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 5. Battlefields Section
                    val filteredBattlefields = battlefields.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    FoldableSection(title = "Battlefields", count = filteredBattlefields.size, icon = Icons.Default.Map, color = BattlefieldColor) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(filteredBattlefields) { battlefield ->
                                PresenterSidebarRow(battlefield.name, BattlefieldColor, icon = Icons.Default.Map, onClick = {
                                    viewModel.addItem(battlefield.name, "Battlefield", imageUrl = battlefield.displayImageUrl, isBackground = true)
                                })
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 6. Events Section
                    val filteredEvents = events.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    FoldableSection(title = "Saved Events", count = filteredEvents.size, icon = Icons.Default.AutoFixHigh, color = MaterialTheme.colorScheme.secondary) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(filteredEvents) { event ->
                                var showSaveDialog by remember { mutableStateOf(false) }
                                EventSidebarRow(
                                    event = event,
                                    onLoad = { viewModel.loadEvent(event) },
                                    onDelete = { viewModel.deleteEvent(event.id) },
                                    onSave = { showSaveDialog = true }
                                )
                                if (showSaveDialog) {
                                    SaveEventDialog(
                                        initialName = event.name,
                                        onDismiss = { showSaveDialog = false },
                                        onSave = { name ->
                                            viewModel.saveCurrentEvent(name, event.id)
                                            showSaveDialog = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 7. Items Section
                    val allItems = distinctChars.flatMap { c -> c.items.map { Triple(it, c.id, c.name) } }
                        .filter { it.first.name.contains(searchQuery, ignoreCase = true) }
                    FoldableSection(title = "Inventory", count = allItems.size, icon = Icons.Default.ShoppingBag, color = ItemColor) {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(allItems) { (item, ownerId, ownerName) ->
                                var itemCoords: androidx.compose.ui.layout.LayoutCoordinates? by remember { mutableStateOf(null) }
                                PresenterSidebarRow(
                                    title = "${item.name} ($ownerName)", 
                                    accentColor = ItemColor, 
                                    icon = itemToIcon(item), 
                                    onClick = {
                                        viewModel.addItem(
                                            title = item.name, 
                                            type = "Item", 
                                            imageUrl = item.imageUrl,
                                            subInfo = "${item.rarity.name} · Owned by $ownerName",
                                            description = item.description
                                        )
                                    },
                                    modifier = Modifier.onGloballyPositioned { itemCoords = it }
                                        .pointerInput(item.id) {
                                            detectDragGestures(
                                                onDragStart = { _ ->
                                                    draggedItemInfo = Triple(item, ownerId, ownerName)
                                                },
                                                onDrag = { change, _ ->
                                                    val base = itemCoords?.boundsInWindow()?.topLeft ?: androidx.compose.ui.geometry.Offset.Zero
                                                    mousePosition = base + change.position
                                                    
                                                    // Check for drop target
                                                    dropTargetId = characterBounds.entries.find { (_, coords) ->
                                                        if (coords.isAttached) {
                                                            coords.boundsInWindow().contains(mousePosition)
                                                        } else false
                                                    }?.key
                                                },
                                                onDragEnd = {
                                                    val targetId = dropTargetId
                                                    if (draggedItemInfo != null && targetId != null && targetId != ownerId) {
                                                        characterListViewModel.moveItemBetweenCharacters(item, ownerId, targetId)
                                                    }
                                                    draggedItemInfo = null
                                                    dropTargetId = null
                                                },
                                                onDragCancel = {
                                                    draggedItemInfo = null
                                                    dropTargetId = null
                                                }
                                            )
                                        }
                                )
                            }
                        }
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
                    .background(ItemColor.copy(alpha = 0.8f), MaterialTheme.shapes.small)
                    .border(1.dp, Color.White, MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Text(item.name, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SaveEventDialog(
    initialName: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Current Event") },
        text = {
            Column {
                Text("Save all currently placed characters, monsters, and backgrounds as a single event.")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name) }, enabled = name.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun EventSidebarRow(
    event: GameEvent,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(event.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text("${event.items.size} elements", style = MaterialTheme.typography.labelSmall)
            }
            Row {
                IconButton(onClick = onSave, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onLoad, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun PresenterSidebarRow(
    title: String, 
    accentColor: Color, 
    icon: ImageVector? = null, 
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = if (isHighlighted) accentColor.copy(alpha = 0.2f) else Color.Transparent,
        border = if (isHighlighted) androidx.compose.foundation.BorderStroke(1.dp, accentColor) else null
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (icon != null) {
                    Icon(icon, null, modifier = Modifier.size(16.dp), tint = accentColor.copy(alpha = 0.7f))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = title, 
                    style = MaterialTheme.typography.bodyMedium, 
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = accentColor)
        }
    }
}

@Composable
private fun FoldableSection(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (expanded) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape) {
                        Icon(icon, null, modifier = Modifier.size(24.dp).padding(4.dp), tint = color)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Surface(color = color.copy(alpha = 0.1f), shape = CircleShape) {
                        Text(
                            count.toString(), 
                            modifier = Modifier.padding(horizontal = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Box(modifier = Modifier.padding(bottom = 8.dp, start = 8.dp, end = 8.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun PlayerViewContent(activeItems: List<PresentedItem>, showStats: Boolean = true) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        WorkspaceContainer(
            modifier = Modifier.fillMaxSize(),
            activeItems = activeItems,
            showStats = showStats,
            isDM = false
        )
    }
}

@Composable
fun WorkspaceContainer(
    modifier: Modifier = Modifier,
    activeItems: List<PresentedItem>,
    onItemPositionChange: (String, Float, Float) -> Unit = { _, _, _ -> },
    onItemSizeChange: (String, Float, Float) -> Unit = { _, _, _ -> },
    onItemRemove: (String) -> Unit = {},
    onItemHpChange: (String, Int) -> Unit = { _, _ -> },
    onItemZoomChange: (String, Float) -> Unit = { _, _ -> },
    onItemOffsetChange: (String, Float, Float) -> Unit = { _, _, _ -> },
    showStats: Boolean = true,
    isDM: Boolean = false
) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()
        
        val scale = minOf(containerWidth / LOGICAL_CANVAS_SIZE, containerHeight / LOGICAL_CANVAS_SIZE)
        val canvasWidthPx = LOGICAL_CANVAS_SIZE * scale
        val canvasHeightPx = LOGICAL_CANVAS_SIZE * scale

        Box(
            modifier = Modifier.size(
                width = (canvasWidthPx / androidx.compose.ui.platform.LocalDensity.current.density).dp,
                height = (canvasHeightPx / androidx.compose.ui.platform.LocalDensity.current.density).dp
            )
        ) {
            val sortedItems = activeItems.sortedByDescending { it.isBackground }
            
            sortedItems.forEach { item ->
                val categoryColor = when(item.type.lowercase()) {
                    "monster" -> MonsterColor
                    "npc" -> NpcColor
                    "location" -> LocationColor
                    "battlefield" -> BattlefieldColor
                    "item" -> ItemColor
                    "character" -> CharacterColor
                    else -> Color.White
                }

                // Detect if this item is "fighting" (near an enemy)
                val isFighting = remember(item, activeItems.size) {
                    derivedStateOf {
                        if (item.isBackground || item.type.lowercase() == "item") false
                        else {
                            activeItems.any { other ->
                                if (other.id == item.id || other.isBackground || other.type.lowercase() == "item") false
                                else {
                                    // Combat logic: Monsters fight Characters/NPCs, and vice versa
                                    val isEnemy = (item.type.lowercase() == "monster" && (other.type.lowercase() == "character" || other.type.lowercase() == "npc")) ||
                                                 (other.type.lowercase() == "monster" && (item.type.lowercase() == "character" || item.type.lowercase() == "npc"))
                                    
                                    if (isEnemy) {
                                        val dx = (item.x + item.width / 2) - (other.x + other.width / 2)
                                        val dy = (item.y + item.height / 2) - (other.y + other.height / 2)
                                        val distance = sqrt(dx * dx + dy * dy)
                                        // Animation threshold is relative to the size of cards
                                        val avgSize = (item.width + item.height + other.width + other.height) / 4
                                        distance < avgSize * 1.2f 
                                    } else false
                                }
                            }
                        }
                    }
                }

                key(item.id) {
                    var showDetails by remember { mutableStateOf(false) }
                    
                    Box {
                        PresentationItem(
                            item = item,
                            canvasSizePx = canvasWidthPx,
                            categoryColor = categoryColor,
                            isFighting = isFighting.value,
                            onPositionChange = { x, y -> onItemPositionChange(item.id, x, y) },
                            onSizeChange = { w, h -> onItemSizeChange(item.id, w, h) },
                            onRemove = { onItemRemove(item.id) },
                            onHpChange = { delta -> onItemHpChange(item.id, delta) },
                            onZoomChange = { delta -> onItemZoomChange(item.id, delta) },
                            onOffsetChange = { dx, dy -> onItemOffsetChange(item.id, dx, dy) },
                            showStats = showStats,
                            isDM = isDM,
                            onToggleDetails = { showDetails = !showDetails }
                        )

                        // DM Detail Overlay
                        if (isDM && showDetails) {
                            val pixelScale = if (canvasWidthPx > 0) canvasWidthPx / LOGICAL_CANVAS_SIZE else 1f
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        translationX = item.x * pixelScale
                                        translationY = item.y * pixelScale
                                    }
                                    .size(
                                        width = (item.width * pixelScale / androidx.compose.ui.platform.LocalDensity.current.density).dp,
                                        height = (item.height * pixelScale / androidx.compose.ui.platform.LocalDensity.current.density).dp
                                    )
                                    .padding(8.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f)),
                                    shape = MaterialTheme.shapes.medium,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, categoryColor.copy(alpha = 0.5f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("DM Notes", style = MaterialTheme.typography.labelMedium, color = categoryColor, fontWeight = FontWeight.Bold)
                                            IconButton(onClick = { showDetails = false }, modifier = Modifier.size(20.dp)) {
                                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        if (!item.subInfo.isNullOrBlank()) {
                                            Text(item.subInfo, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.2f))
                                        
                                        // Dynamic content based on type could be added here
                                        Text(
                                            text = item.description ?: "No description provided.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.8f),
                                            lineHeight = 14.sp
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

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun PresentationItem(
    item: PresentedItem,
    canvasSizePx: Float,
    categoryColor: Color = Color.White,
    isFighting: Boolean = false,
    onPositionChange: (Float, Float) -> Unit = { _, _ -> },
    onSizeChange: (Float, Float) -> Unit = { _, _ -> },
    onRemove: () -> Unit = {},
    onHpChange: (Int) -> Unit = {},
    onZoomChange: (Float) -> Unit = {},
    onOffsetChange: (Float, Float) -> Unit = { _, _ -> },
    showStats: Boolean = true,
    isDM: Boolean = false,
    onToggleDetails: () -> Unit = {}
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val pixelScale = if (canvasSizePx > 0) canvasSizePx / LOGICAL_CANVAS_SIZE else 1f
    
    var localX by remember { mutableStateOf(item.x) }
    var localY by remember { mutableStateOf(item.y) }
    var localW by remember { mutableStateOf(item.width) }
    var localH by remember { mutableStateOf(item.height) }

    LaunchedEffect(item.x, item.y) { localX = item.x; localY = item.y }
    LaunchedEffect(item.width, item.height) { localW = item.width; localH = item.height }

    val currentOnPositionChange by rememberUpdatedState(onPositionChange)
    val currentOnSizeChange by rememberUpdatedState(onSizeChange)
    
    val focusRequester = remember { FocusRequester() }

    // Shake animation for "fighting" state
    val infiniteTransition = rememberInfiniteTransition(label = "shake")
    val shakeX by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeX"
    )
    val shakeY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(40, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shakeY"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = (localX * pixelScale) + (if (isFighting) shakeX * density.density else 0f)
                translationY = (localY * pixelScale) + (if (isFighting) shakeY * density.density else 0f)
                scaleX = if (isFighting) 1.05f else 1f
                scaleY = if (isFighting) 1.05f else 1f
            }
            .size(
                width = (localW * pixelScale / density.density).dp,
                height = (localH * pixelScale / density.density).dp
            )
            .then(
                if (isDM) {
                    Modifier
                        .onKeyEvent { keyEvent ->
                            if ((item.type.lowercase() == "location" || item.type.lowercase() == "battlefield") && keyEvent.type == KeyEventType.KeyDown) {
                                when (keyEvent.key) {
                                    Key.Equals, Key.NumPadAdd -> { onZoomChange(0.1f); true }
                                    Key.Minus, Key.NumPadSubtract -> { onZoomChange(-0.1f); true }
                                    Key.W -> { onOffsetChange(0f, 0.05f); true }
                                    Key.S -> { onOffsetChange(0f, -0.05f); true }
                                    Key.A -> { onOffsetChange(0.05f, 0f); true }
                                    Key.D -> { onOffsetChange(-0.05f, 0f); true }
                                    else -> false
                                }
                            } else false
                        }
                        .focusRequester(focusRequester)
                        .focusable()
                        .pointerInput(item.id) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    
                                    // 1. Scroll Zoom
                                    if ((item.type.lowercase() == "location" || item.type.lowercase() == "battlefield") && event.type == PointerEventType.Scroll) {
                                        val delta = event.changes.first().scrollDelta.y
                                        if (delta != 0f) {
                                            val zoomDelta = if (delta > 0) -0.1f else 0.1f
                                            onZoomChange(zoomDelta)
                                        }
                                    }

                                    // 2. Drag / Pan (Move while pressed)
                                    if (event.type == PointerEventType.Move) {
                                        val change = event.changes.firstOrNull()
                                        if (change != null && change.pressed) {
                                            val isRightClick = event.buttons.isSecondaryPressed
                                            val dragAmount = change.positionChange()
                                            
                                            if (dragAmount != androidx.compose.ui.geometry.Offset.Zero) {
                                                change.consume()
                                                if ((item.type.lowercase() == "location" || item.type.lowercase() == "battlefield") && isRightClick) {
                                                    onOffsetChange(dragAmount.x / 400f, dragAmount.y / 400f)
                                                } else if (!isRightClick) {
                                                    val logicalDeltaX = dragAmount.x / pixelScale
                                                    val logicalDeltaY = dragAmount.y / pixelScale
                                                    localX = (localX + logicalDeltaX).coerceIn(0f, LOGICAL_CANVAS_SIZE - localW)
                                                    localY = (localY + logicalDeltaY).coerceIn(0f, LOGICAL_CANVAS_SIZE - localH)
                                                    currentOnPositionChange(localX, localY)
                                                }
                                            }
                                        }
                                    }
                                    
                                    // 3. Focus on press
                                    if (event.type == PointerEventType.Press) {
                                        focusRequester.requestFocus()
                                    }
                                }
                            }
                        }
                } else Modifier
            )
    ) {
        // Halo / Aura Effect
        if (!item.isBackground) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .blur(12.dp)
                    .background(categoryColor.copy(alpha = 0.4f), MaterialTheme.shapes.large)
            )
        }

        if (item.isBackground) {
            LocationCard(item, isDM, onRemove)
        } else {
            PlayerCard(item, categoryColor, isDM, onRemove, onHpChange, showStats, onToggleDetails)
            
            // Crossing Swords Effect
            if (isFighting) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ClashingSwords()
                }
            }
        }

        if (isDM) {
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).size(32.dp)
                    .pointerInput(item.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val logicalDeltaW = dragAmount.x / pixelScale
                            val logicalDeltaH = dragAmount.y / pixelScale
                            localW = (localW + logicalDeltaW).coerceIn(50f, LOGICAL_CANVAS_SIZE - localX)
                            localH = (localH + logicalDeltaH).coerceIn(50f, LOGICAL_CANVAS_SIZE - localY)
                            currentOnSizeChange(localW, localH)
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.SouthEast,
                    contentDescription = "Resize",
                    modifier = Modifier.size(20.dp).align(Alignment.BottomEnd).padding(4.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun ClashingSwords() {
    val infiniteTransition = rememberInfiniteTransition(label = "clash")
    val rotation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.scale(scale)) {
        Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            modifier = Modifier.size(48.dp).rotate(45f + rotation),
            tint = Color.White.copy(alpha = 0.8f)
        )
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = null,
            modifier = Modifier.size(48.dp).rotate(-45f - rotation),
            tint = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun PlayerCard(
    item: PresentedItem,
    categoryColor: Color = Color.White,
    isDM: Boolean = false,
    onRemove: () -> Unit = {},
    onHpChange: (Int) -> Unit = {},
    showStats: Boolean = true,
    onToggleDetails: () -> Unit = {}
) {
    val title = item.title
    val imageUrl = item.imageUrl
    
    val shape = MaterialTheme.shapes.large
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E1E1E),
        shadowElevation = if (isDM) 8.dp else 4.dp,
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(2.dp, categoryColor.copy(alpha = 0.8f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.6f).align(Alignment.Center),
                    tint = categoryColor.copy(alpha = 0.3f)
                )
            }

            // Bottom info area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f), Color.Black)
                        )
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                if (!item.subInfo.isNullOrBlank()) {
                    Text(
                        text = item.subInfo,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }

                if (showStats && item.currentHp != null && item.maxHp != null) {
                    Spacer(Modifier.height(8.dp))
                    val hpRatio = (item.currentHp.toFloat() / item.maxHp).coerceIn(0f, 1f)
                    val hpColor = when {
                        hpRatio > 0.5f -> Color(0xFF66BB6A)
                        hpRatio > 0.2f -> Color(0xFFFFA726)
                        else -> Color(0xFFEF5350)
                    }

                    // Pulse effect for low health
                    val hpAlpha = if (hpRatio <= 0.2f) {
                        val infiniteTransition = rememberInfiniteTransition(label = "hpPulse")
                        infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "alpha"
                        ).value
                    } else 1f
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        if (isDM) {
                            Surface(
                                onClick = { onHpChange(-1) },
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (hpRatio > 0.2f) Icons.Default.Favorite else Icons.Default.HeartBroken,
                                contentDescription = null,
                                tint = hpColor.copy(alpha = hpAlpha),
                                modifier = Modifier.size(12.dp).align(Alignment.CenterStart)
                            )
                            LinearProgressIndicator(
                                progress = { hpRatio },
                                modifier = Modifier
                                    .padding(start = 18.dp, end = 24.dp)
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .align(Alignment.Center),
                                color = hpColor,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                            Text(
                                "${item.currentHp}", 
                                style = MaterialTheme.typography.labelSmall, 
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }

                        if (isDM) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                onClick = { onHpChange(1) },
                                modifier = Modifier.size(32.dp),
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.3f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            // AC Badge
            if (showStats && item.armorClass != null) {
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AC", fontSize = 7.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                            Text(item.armorClass.toString(), fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            // Stats row (small pills at top)
            if (showStats && item.stats != null) {
                Column(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatPill("S", item.stats.strength)
                        StatPill("D", item.stats.dexterity)
                        StatPill("C", item.stats.constitution)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        StatPill("I", item.stats.intelligence)
                        StatPill("W", item.stats.wisdom)
                        StatPill("C", item.stats.charisma)
                    }
                }
            }

            if (isDM) {
                Row(
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onToggleDetails,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp), tint = Color.White)
                    }
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: Int) {
    Surface(
        color = Color.Black.copy(alpha = 0.6f),
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 8.sp, color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(2.dp))
            Text(value.toString(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LocationCard(item: PresentedItem, isDM: Boolean = false, onRemove: () -> Unit = {}) {
    val title = item.title
    val imageUrl = item.imageUrl
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212),
        shape = MaterialTheme.shapes.extraSmall,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = item.zoom
                            scaleY = item.zoom
                            translationX = item.offsetX * size.width
                            translationY = item.offsetY * size.height
                        },
                    contentScale = ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(4.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.3f),
                        tint = Color.DarkGray
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                }
            }
            
            if (isDM) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = if (!imageUrl.isNullOrBlank()) Color.White else Color.Gray)
                }
            }
        }
    }
}
