package com.dnd.helper.presentation.characterdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    viewModel: CharacterDetailViewModel,
    onBackClick: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.character?.name ?: "Character") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.SaveChanges) }) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                        }
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.ToggleEdit) }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                        }
                    } else {
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.ToggleEdit) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                        IconButton(onClick = { viewModel.onEvent(CharacterDetailEvent.Refresh) }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else if (state.error != null) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            } else {
                state.character?.let { character ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (character.imageUrl != null) {
                            AsyncImage(
                                model = character.imageUrl,
                                contentDescription = character.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .background(Color.Black),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            if (state.isEditing) {
                                state.editedCharacter?.let { edited ->
                                    OutlinedTextField(
                                        value = edited.name,
                                        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(name = it))) },
                                        label = { Text("Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = edited.race,
                                            onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(race = it))) },
                                            label = { Text("Race") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = edited.characterClass,
                                            onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(characterClass = it))) },
                                            label = { Text("Class") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = edited.playerName,
                                        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(playerName = it))) },
                                        label = { Text("Player Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = edited.imageUrl ?: "",
                                        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(imageUrl = it.ifBlank { null }))) },
                                        label = { Text("Image URL") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = edited.description,
                                        onValueChange = { viewModel.onEvent(CharacterDetailEvent.EditCharacter(edited.copy(description = it))) },
                                        label = { Text("Description") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )
                                }
                            } else {
                                Text(
                                    text = character.name,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${character.race} ${character.characterClass}, Level ${character.level}",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedIconButton(
                                        onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateLevel(-1)) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(14.dp))
                                    }
                                    OutlinedIconButton(
                                        onClick = { viewModel.onEvent(CharacterDetailEvent.UpdateLevel(1)) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Text(
                                    text = "Player: ${character.playerName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Description",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                Text(
                                    text = character.description,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Stats",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            StatRow(
                                label = "HP",
                                value = "${character.currentHp} / ${character.maxHp}",
                                onIncrement = { viewModel.onEvent(CharacterDetailEvent.UpdateHp(1)) },
                                onDecrement = { viewModel.onEvent(CharacterDetailEvent.UpdateHp(-1)) }
                            )
                            StatRow(
                                label = "STR",
                                value = character.stats.strength.toString(),
                                onIncrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("strength", 1)) },
                                onDecrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("strength", -1)) }
                            )
                            StatRow(
                                label = "DEX",
                                value = character.stats.dexterity.toString(),
                                onIncrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("dexterity", 1)) },
                                onDecrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("dexterity", -1)) }
                            )
                            StatRow(
                                label = "CON",
                                value = character.stats.constitution.toString(),
                                onIncrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("constitution", 1)) },
                                onDecrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("constitution", -1)) }
                            )
                            StatRow(
                                label = "INT",
                                value = character.stats.intelligence.toString(),
                                onIncrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("intelligence", 1)) },
                                onDecrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("intelligence", -1)) }
                            )
                            StatRow(
                                label = "WIS",
                                value = character.stats.wisdom.toString(),
                                onIncrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("wisdom", 1)) },
                                onDecrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("wisdom", -1)) }
                            )
                            StatRow(
                                label = "CHA",
                                value = character.stats.charisma.toString(),
                                onIncrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("charisma", 1)) },
                                onDecrement = { viewModel.onEvent(CharacterDetailEvent.UpdateStat("charisma", -1)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedIconButton(
                onClick = onDecrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease",
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedIconButton(
                onClick = onIncrement,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
