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
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.presentation.charactercreate.CharacterCreateScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.random.Random

sealed class CreatorType(val title: String, val icon: ImageVector) {
    data object Character : CreatorType("Character", Icons.Default.PersonAdd)
    data object Item : CreatorType("Item", Icons.Default.AddBox)
    data object Monster : CreatorType("Monster", Icons.Default.BugReport)
    data object Location : CreatorType("Location", Icons.Default.AddLocation)
}

@Composable
fun CreatorScreen() {
    var selectedType by remember { mutableStateOf<CreatorType?>(null) }

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
                    else -> PlaceholderScreen("Creation Form for ${selectedType!!.title}")
                }
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
