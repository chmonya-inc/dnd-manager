package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

sealed class LibraryType(val title: String, val icon: ImageVector) {
    data object Items : LibraryType("Items", Icons.Default.ShoppingBag)
    data object Mobs : LibraryType("Monsters", Icons.Default.BugReport) 
    data object Locations : LibraryType("Locations", Icons.Default.Map)
}

@Composable
fun LibraryScreen(
    presentationViewModel: PresentationViewModel = koinViewModel()
) {
    var selectedType by remember { mutableStateOf<LibraryType>(LibraryType.Items) }
    val repository: CharacterRepository = koinInject()
    val scope = rememberCoroutineScope()
    
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedType) {
        if (selectedType == LibraryType.Locations) {
            isLoading = true
            val result = repository.getLocations()
            if (result is com.dnd.helper.domain.common.Result.Success) {
                locations = result.data
            }
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = when(selectedType) {
                LibraryType.Items -> 0
                LibraryType.Mobs -> 1
                LibraryType.Locations -> 2
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                selected = selectedType == LibraryType.Locations,
                onClick = { selectedType = LibraryType.Locations },
                text = { Text(LibraryType.Locations.title) },
                icon = { Icon(LibraryType.Locations.icon, null) }
            )
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                when (selectedType) {
                    LibraryType.Items -> EntityGrid("Items", Icons.Default.ShoppingBag, presentationViewModel, false)
                    LibraryType.Mobs -> EntityGrid("Monsters", Icons.Default.BugReport, presentationViewModel, false)
                    LibraryType.Locations -> LocationGrid(locations, presentationViewModel, onDelete = { id ->
                        scope.launch {
                            repository.deleteLocation(id)
                            locations = locations.filter { it.id != id }
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun LocationGrid(
    locations: List<Location>,
    presentationViewModel: PresentationViewModel,
    onDelete: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Locations", style = MaterialTheme.typography.headlineSmall)
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
                        onDelete = { onDelete(location.id) }
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
    onDelete: () -> Unit
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
                    IconButton(onClick = { /* TODO: Edit */ }) {
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
private fun EntityGrid(
    title: String, 
    icon: ImageVector,
    presentationViewModel: PresentationViewModel,
    isLocation: Boolean
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            OutlinedButton(onClick = { /* TODO: Open Creator with this type */ }) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text("New $title")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Placeholder for real data from repository
        val placeholders = List(12) { "$title #$it" }
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 180.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(placeholders) { name ->
                EntityCard(name, icon, onPresent = { presentationViewModel.addItem(name, type = title, isBackground = isLocation) })
            }
        }
    }
}

@Composable
private fun EntityCard(name: String, icon: ImageVector, onPresent: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { /* TODO: Edit */ }) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = { /* TODO: Delete */ }) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onPresent) {
                    Icon(Icons.Default.Tv, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
