package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.Character
import com.dnd.helper.presentation.characterlist.CharacterListViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@Composable
fun PresentationScreen(
    viewModel: PresentationViewModel = koinViewModel(),
    characterListViewModel: CharacterListViewModel = koinViewModel()
) {
    val isWindowOpen by viewModel.isWindowOpen.collectAsState()
    val activeItems = viewModel.activeItems
    val characterListState by characterListViewModel.state.collectAsState()

    DisposableEffect(characterListViewModel) {
        characterListViewModel.startAutoRefresh()
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
                Text("Presentation Workspace (Drag items here)", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .border(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    val containerWidthPx = constraints.maxWidth.toFloat()
                    val containerHeightPx = constraints.maxHeight.toFloat()

                    // Sorting: Backgrounds at back, Tokens on top
                    val sortedItems = activeItems.sortedByDescending { it.isBackground }
                    
                    sortedItems.forEach { item ->
                        key(item.id) {
                            PresentationItem(
                                item = item,
                                containerWidthPx = containerWidthPx,
                                containerHeightPx = containerHeightPx,
                                onPositionChange = { x, y -> viewModel.updatePosition(item.id, x, y) },
                                onSizeChange = { w, h -> viewModel.updateSize(item.id, w, h) },
                                onRemove = { viewModel.removeItem(item.id) },
                                isDM = true
                            )
                        }
                    }
                }
            }
            
            // Sidebar
            Column(modifier = Modifier.weight(0.35f)) {
                Text("Quick Actions", style = MaterialTheme.typography.titleLarge)
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
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Quick Items", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        
                        TextButton(onClick = { 
                            viewModel.addItem("Cave Entrance", "Location", imageUrl = "https://i.pinimg.com/736x/2b/4c/d1/2b4cd1ca5970c6778f3f889989600e12.jpg", isBackground = true) 
                        }) {
                            Icon(Icons.Default.Map, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Map: Cave Entrance")
                        }
                        
                        TextButton(onClick = { 
                            viewModel.addItem("Beholder", "Monster", imageUrl = "https://www.dndbeyond.com/avatars/thumbnails/30745/38/1000/1000/638061183248695027.png") 
                        }) {
                            Icon(Icons.Default.BugReport, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Monster: Beholder")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
                        Text("Add Characters", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        if (characterListState.isLoading && characterListState.characters.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else {
                            val uniqueCharacters = characterListState.characters.distinctBy { it.id }
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(
                                    items = uniqueCharacters,
                                    key = { it.id }
                                ) { character: Character ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = character.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        IconButton(
                                            onClick = { viewModel.addItem(character.name, "Character", imageUrl = character.displayImageUrl) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Add Locations Section
                var locations by remember { mutableStateOf<List<com.dnd.helper.domain.model.Location>>(emptyList()) }
                var locationsLoading by remember { mutableStateOf(false) }
                val repository: com.dnd.helper.domain.repository.CharacterRepository = org.koin.compose.koinInject()

                LaunchedEffect(Unit) {
                    locationsLoading = true
                    val result = repository.getLocations()
                    if (result is com.dnd.helper.domain.common.Result.Success) {
                        locations = result.data
                    }
                    locationsLoading = false
                }

                Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
                        Text("Add Locations", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))

                        if (locationsLoading) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(locations, key = { it.id }) { location ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = location.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        IconButton(
                                            onClick = { viewModel.addItem(location.name, "Location", imageUrl = location.displayImageUrl, isBackground = true) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
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
}

@Composable
fun PlayerViewContent(activeItems: List<PresentedItem>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val containerWidthPx = constraints.maxWidth.toFloat()
            val containerHeightPx = constraints.maxHeight.toFloat()

            val sortedItems = activeItems.sortedByDescending { it.isBackground }
            
            sortedItems.forEach { item ->
                key(item.id) {
                    PresentationItem(
                        item = item,
                        containerWidthPx = containerWidthPx,
                        containerHeightPx = containerHeightPx,
                        isDM = false
                    )
                }
            }
        }
    }
}

@Composable
fun PresentationItem(
    item: PresentedItem,
    containerWidthPx: Float,
    containerHeightPx: Float,
    onPositionChange: (Float, Float) -> Unit = { _, _ -> },
    onSizeChange: (Float, Float) -> Unit = { _, _ -> },
    onRemove: () -> Unit = {},
    isDM: Boolean = false
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    
    // Crucial: Use rememberUpdatedState to avoid capturing stale values in the gesture listener
    val currentItem by rememberUpdatedState(item)
    val currentW by rememberUpdatedState(containerWidthPx)
    val currentH by rememberUpdatedState(containerHeightPx)
    
    // Offset lambda ensures the move doesn't trigger a full recomposition of the item's content
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    (currentItem.x * currentW).roundToInt(),
                    (currentItem.y * currentH).roundToInt()
                )
            }
            .size(currentItem.width.dp, currentItem.height.dp)
            .then(
                if (isDM) {
                    Modifier.pointerInput(currentItem.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            
                            // 1. Convert pixel drag to proportional delta
                            val deltaX = dragAmount.x / currentW
                            val deltaY = dragAmount.y / currentH
                            
                            // 2. Calculate item's current proportional size
                            val itemWidthPx = with(density) { currentItem.width.dp.toPx() }
                            val itemHeightPx = with(density) { currentItem.height.dp.toPx() }
                            val propW = if (currentW > 0) itemWidthPx / currentW else 0f
                            val propH = if (currentH > 0) itemHeightPx / currentH else 0f
                            
                            // 3. Apply strict boundaries on all 4 sides
                            val newX = (currentItem.x + deltaX).coerceIn(0f, (1f - propW).coerceAtLeast(0f))
                            val newY = (currentItem.y + deltaY).coerceIn(0f, (1f - propH).coerceAtLeast(0f))
                            
                            onPositionChange(newX, newY)
                        }
                    }
                } else Modifier
            )
    ) {
        if (currentItem.isBackground) {
            LocationCard(currentItem.title, currentItem.imageUrl, isDM, onRemove)
        } else {
            PlayerCard(currentItem.title, currentItem.imageUrl, isDM, onRemove)
        }

        // Resize handle for DM
        if (isDM) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp) // Larger hit area
                    .pointerInput(currentItem.id) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            
                            // 1:1 Resize in Dp
                            val deltaWDp = dragAmount.x / density.density
                            val deltaHDp = dragAmount.y / density.density
                            
                            val nextWDp = currentItem.width + deltaWDp
                            val nextHDp = currentItem.height + deltaHDp
                            
                            // Boundary: don't let it grow past the workspace edge
                            val maxWPx = currentW * (1f - currentItem.x)
                            val maxHPx = currentH * (1f - currentItem.y)
                            val maxWDp = with(density) { maxWPx.toDp().value }
                            val maxHDp = with(density) { maxHPx.toDp().value }
                            
                            onSizeChange(
                                nextWDp.coerceIn(60f, maxWDp),
                                nextHDp.coerceIn(60f, maxHDp)
                            )
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
