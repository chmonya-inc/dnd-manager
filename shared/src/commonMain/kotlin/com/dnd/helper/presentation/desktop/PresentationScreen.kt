package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.presentation.characterlist.CharacterListViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val LOGICAL_CANVAS_SIZE = 1000f

@Composable
fun PresentationScreen(
    viewModel: PresentationViewModel = koinViewModel(),
    characterListViewModel: CharacterListViewModel = koinViewModel()
) {
    val isWindowOpen by viewModel.isWindowOpen.collectAsState()
    val activeItems = viewModel.activeItems
    val characterListState by characterListViewModel.state.collectAsState()
    val repository: CharacterRepository = koinInject()

    var monsters by remember { mutableStateOf<List<Monster>>(emptyList()) }
    var npcs by remember { mutableStateOf<List<Npc>>(emptyList()) }
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var isDataLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val refreshData = {
        scope.launch {
            isDataLoading = true
            val mResult = repository.getMonsters()
            if (mResult is com.dnd.helper.domain.common.Result.Success) monsters = mResult.data
            val nResult = repository.getNpcs()
            if (nResult is com.dnd.helper.domain.common.Result.Success) npcs = nResult.data
            val lResult = repository.getLocations()
            if (lResult is com.dnd.helper.domain.common.Result.Success) locations = lResult.data
            isDataLoading = false
        }
    }

    LaunchedEffect(Unit) {
        characterListViewModel.startAutoRefresh()
        refreshData()
    }

    DisposableEffect(characterListViewModel) {
        onDispose { characterListViewModel.stopAutoRefresh() }
    }

    ExternalWindow(
        isOpen = isWindowOpen,
        onCloseRequest = { viewModel.setWindowOpen(false) }
    ) {
        com.dnd.helper.theme.DndHelperTheme {
            PlayerViewContent(activeItems = activeItems)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Presentation Controller", style = MaterialTheme.typography.headlineSmall)
                Text(
                    text = if (isWindowOpen) "Window is active" else "Window is closed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isWindowOpen) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
            
            Button(
                onClick = { viewModel.toggleWindow() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isWindowOpen) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(if (isWindowOpen) Icons.Default.Stop else Icons.Default.PlayArrow, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isWindowOpen) "Close Player View" else "Open Player View")
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Workspace
            Column(modifier = Modifier.weight(1f)) {
                Text("Workspace (Drag items here)", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                
                WorkspaceContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .border(1.dp, MaterialTheme.colorScheme.outline),
                    activeItems = activeItems,
                    onItemPositionChange = viewModel::updatePosition,
                    onItemSizeChange = viewModel::updateSize,
                    onItemRemove = viewModel::removeItem,
                    isDM = true
                )
            }
            
            // Sidebar
            Column(modifier = Modifier.weight(0.4f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Library Sections", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Libraries")
                    }
                }
                Spacer(Modifier.height(12.dp))
                
                Button(
                    onClick = { viewModel.clearItems() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = activeItems.isNotEmpty()
                ) {
                    Icon(Icons.Default.ClearAll, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Clear Workspace")
                }
                
                Spacer(Modifier.height(16.dp))

                val distinctChars = characterListState.characters.distinctBy { it.id }
                
                // 1. Characters Section
                FoldableSection(title = "Characters (${distinctChars.size})", icon = Icons.Default.Groups) {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(distinctChars) { character ->
                            PresenterSidebarRow(character.name, onClick = {
                                viewModel.addItem(character.name, "Character", imageUrl = character.displayImageUrl)
                            })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 2. Monsters Section
                FoldableSection(title = "Monsters (${monsters.size})", icon = Icons.Default.BugReport) {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(monsters) { monster ->
                            PresenterSidebarRow(monster.name, onClick = {
                                viewModel.addItem(monster.name, "Monster", imageUrl = monster.displayImageUrl)
                            })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 3. NPCs Section
                FoldableSection(title = "NPCs (${npcs.size})", icon = Icons.Default.EmojiPeople) {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(npcs) { npc ->
                            PresenterSidebarRow(npc.name, onClick = {
                                viewModel.addItem(npc.name, "NPC", imageUrl = npc.displayImageUrl)
                            })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 4. Locations Section
                FoldableSection(title = "Locations (${locations.size})", icon = Icons.Default.Map) {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(locations) { location ->
                            PresenterSidebarRow(location.name, onClick = {
                                viewModel.addItem(location.name, "Location", imageUrl = location.displayImageUrl, isBackground = true)
                            })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 5. Items Section
                val allItems = distinctChars.flatMap { c -> c.items.map { it to c.name } }
                FoldableSection(title = "Items (${allItems.size})", icon = Icons.Default.ShoppingBag) {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(allItems) { (item, owner) ->
                            PresenterSidebarRow("${item.name} ($owner)", onClick = {
                                viewModel.addItem(item.name, "Item", imageUrl = item.imageUrl)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresenterSidebarRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 6.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1)
        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun FoldableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
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
fun PlayerViewContent(activeItems: List<PresentedItem>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        WorkspaceContainer(
            modifier = Modifier.fillMaxSize(),
            activeItems = activeItems,
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
                key(item.id) {
                    PresentationItem(
                        item = item,
                        canvasSizePx = canvasWidthPx,
                        onPositionChange = { x, y -> onItemPositionChange(item.id, x, y) },
                        onSizeChange = { w, h -> onItemSizeChange(item.id, w, h) },
                        onRemove = { onItemRemove(item.id) },
                        isDM = isDM
                    )
                }
            }
        }
    }
}

@Composable
fun PresentationItem(
    item: PresentedItem,
    canvasSizePx: Float,
    onPositionChange: (Float, Float) -> Unit = { _, _ -> },
    onSizeChange: (Float, Float) -> Unit = { _, _ -> },
    onRemove: () -> Unit = {},
    isDM: Boolean = false
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

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = localX * pixelScale
                translationY = localY * pixelScale
            }
            .size(
                width = (localW * pixelScale / density.density).dp,
                height = (localH * pixelScale / density.density).dp
            )
            .then(
                if (isDM) {
                    Modifier.pointerInput(item.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val logicalDeltaX = dragAmount.x / pixelScale
                            val logicalDeltaY = dragAmount.y / pixelScale
                            localX = (localX + logicalDeltaX).coerceIn(0f, LOGICAL_CANVAS_SIZE - localW)
                            localY = (localY + logicalDeltaY).coerceIn(0f, LOGICAL_CANVAS_SIZE - localH)
                            currentOnPositionChange(localX, localY)
                        }
                    }
                } else Modifier
            )
    ) {
        if (item.isBackground) {
            LocationCard(item.title, item.imageUrl, isDM, onRemove)
        } else {
            PlayerCard(item.title, item.imageUrl, isDM, onRemove)
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
fun PlayerCard(title: String, imageUrl: String? = null, isDM: Boolean = false, onRemove: () -> Unit = {}) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E1E1E),
        shadowElevation = if (isDM) 8.dp else 4.dp,
        shape = shape,
        border = if (isDM) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    fontSize = 10.sp
                )
            }

            if (isDM) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp).align(Alignment.TopEnd).padding(2.dp)
                ) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp), tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun LocationCard(title: String, imageUrl: String? = null, isDM: Boolean = false, onRemove: () -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize(),
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
