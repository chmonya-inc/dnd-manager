package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnd.helper.theme.DndIcons
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignPickerScreen(
    onCampaignSelected: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: SessionsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create New Campaign") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Campaign Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addCampaign(name, "")
                            showAddDialog = false
                        }
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Pane: List
            Column(
                modifier = Modifier
                    .width(350.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Your Campaigns",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add Campaign")
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.campaigns) { campaign ->
                            val isSelected = state.previewCampaignId == campaign.id
                            Card(
                                onClick = { viewModel.selectCampaignForPreview(campaign.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(campaign.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "ID: ${campaign.id.take(8)}...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.ChevronRight, null)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(DndIcons.Filled.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout")
                }
            }

            // Vertical Divider
            VerticalDivider()

            // Right Pane: Preview
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val previewId = state.previewCampaignId
                val campaign = state.campaigns.find { it.id == previewId }
                
                if (campaign != null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    campaign.name,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Session ID: ${campaign.id}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.selectCampaign(campaign.id)
                                    onCampaignSelected(campaign.id)
                                },
                                modifier = Modifier.height(56.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(Icons.Default.PlayArrow, null)
                                Spacer(Modifier.width(12.dp))
                                Text("Open Campaign", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(32.dp))

                        if (state.isPreviewLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else if (state.previewData != null) {
                            val data = state.previewData!!
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                                PreviewStatCard(
                                    title = "Characters",
                                    count = data.characters.size,
                                    icon = DndIcons.Filled.People,
                                    modifier = Modifier.weight(1f)
                                )
                                PreviewStatCard(
                                    title = "Locations",
                                    count = data.locations.size,
                                    icon = DndIcons.Filled.Explore,
                                    modifier = Modifier.weight(1f)
                                )
                                PreviewStatCard(
                                    title = "Monsters",
                                    count = data.monsters.size,
                                    icon = DndIcons.Filled.BugReport,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(Modifier.height(32.dp))

                            Text("Quick Summary", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (data.characters.isNotEmpty()) {
                                        Text("Recent Heroes: ${data.characters.take(3).joinToString { it.name }}", style = MaterialTheme.typography.bodyLarge)
                                    }
                                    if (data.npcs.isNotEmpty()) {
                                        Text("Key NPCs: ${data.npcs.take(3).joinToString { it.name }}", style = MaterialTheme.typography.bodyLarge)
                                    }
                                    Text("Campaign ID for players: ${com.dnd.helper.domain.common.IdUtils.encode(campaign.id)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Select a campaign to preview",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewStatCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(count.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold)
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}
