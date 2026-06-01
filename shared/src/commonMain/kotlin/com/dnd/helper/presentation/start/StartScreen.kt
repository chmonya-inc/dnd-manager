package com.dnd.helper.presentation.start

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnd.helper.presentation.desktop.ThemeDialog
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StartScreen(
    onLoadCharacter: (String) -> Unit,
    viewModel: StartViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeDialog(onDismiss = { showThemeDialog = false })
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(
                onClick = { showThemeDialog = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Palette, "Theme")
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "D&D Helper",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Enter your Character ID and Session Table to begin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                OutlinedTextField(
                    value = state.characterId,
                    onValueChange = { viewModel.onEvent(StartEvent.CharacterIdChanged(it)) },
                    label = { Text("Character ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (state.characterId.isNotBlank()) {
                            IconButton(onClick = { viewModel.onEvent(StartEvent.CharacterIdChanged("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.tableId,
                    onValueChange = { viewModel.onEvent(StartEvent.TableIdChanged(it)) },
                    label = { Text("Game ID or Spreadsheet ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if (state.tableId.isNotBlank()) {
                            IconButton(onClick = { viewModel.onEvent(StartEvent.TableIdChanged("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        viewModel.onEvent(StartEvent.LoadCharacter)
                        onLoadCharacter(state.characterId)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.characterId.isNotBlank()
                ) {
                    Text("Load Character")
                }
            }
        }
    }
}
