package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnd.helper.domain.model.Character
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
    var transferDialogData by remember { mutableStateOf<TransferDialogData?>(null) }

    // Drag state: which character is being dragged and which campaign it's hovering over
    var draggedCharacterId by remember { mutableStateOf<String?>(null) }
    var dragOverCampaignId by remember { mutableStateOf<String?>(null) }

    // --- Add Campaign Dialog ---
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

    // --- Transfer / Copy Dialog ---
    transferDialogData?.let { data ->
        TransferCopyDialog(
            character = data.character,
            sourceCampaignName = state.campaigns.find { it.id == data.sourceSessionId }?.name ?: data.sourceSessionId,
            targetCampaign = data.targetCampaign,
            onDismiss = { transferDialogData = null },
            onConfirm = { isCopy, transferItems ->
                viewModel.transferOrCopyCharacter(
                    character = data.character,
                    sourceSessionId = data.sourceSessionId,
                    targetCampaign = data.targetCampaign,
                    isCopy = isCopy,
                    transferItems = transferItems
                )
                transferDialogData = null
            }
        )
    }

    if (state.isTransferring) {
        // Simple progress overlay
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Processing...") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("Transferring character...")
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
                            val isDragTarget = dragOverCampaignId == campaign.id && draggedCharacterId != null
                            Card(
                                onClick = { viewModel.selectCampaignForPreview(campaign.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isDragTarget) {
                                            Modifier.border(
                                                3.dp,
                                                MaterialTheme.colorScheme.primary,
                                                RoundedCornerShape(12.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDragTarget) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .pointerInput(campaign.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    // Highlight campaign as drop target
                                                    dragOverCampaignId = campaign.id
                                                },
                                                onDragEnd = {
                                                    if (draggedCharacterId != null && dragOverCampaignId == campaign.id) {
                                                        // Find character and trigger transfer dialog
                                                        val sourceCampaignId = state.previewCampaignId
                                                        val characters = state.previewData?.characters ?: emptyList()
                                                        val character = characters.find { it.id == draggedCharacterId }
                                                        if (character != null && sourceCampaignId != null && sourceCampaignId != campaign.id) {
                                                            transferDialogData = TransferDialogData(
                                                                character = character,
                                                                sourceSessionId = sourceCampaignId,
                                                                targetCampaign = campaign
                                                            )
                                                        }
                                                    }
                                                    draggedCharacterId = null
                                                    dragOverCampaignId = null
                                                },
                                                onDragCancel = {
                                                    draggedCharacterId = null
                                                    dragOverCampaignId = null
                                                }
                                            )
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(campaign.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "ID: ${campaign.id.take(8)}...",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isDragTarget) {
                                            Text(
                                                "Drop here",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (isSelected && !isDragTarget) {
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
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Game Started: ", style = MaterialTheme.typography.bodyMedium)
                                    Switch(
                                        checked = campaign.isStarted,
                                        onCheckedChange = { viewModel.toggleCampaignStart(campaign.id, it) }
                                    )
                                }
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(32.dp)
                            ) {
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

                            // Character list section
                            Text(
                                "Characters in Campaign",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))

                            if (data.characters.isEmpty()) {
                                Text(
                                    "No characters in this campaign yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(data.characters) { character ->
                                        CharacterRow(
                                            character = character,
                                            otherCampaigns = state.campaigns.filter { it.id != campaign.id },
                                            isDragging = draggedCharacterId == character.id,
                                            onDragStart = {
                                                draggedCharacterId = character.id
                                                dragOverCampaignId = null
                                            },
                                            onDragEnd = {
                                                draggedCharacterId = null
                                                dragOverCampaignId = null
                                            },
                                            onTransferClick = { targetCampaign ->
                                                transferDialogData = TransferDialogData(
                                                    character = character,
                                                    sourceSessionId = campaign.id,
                                                    targetCampaign = targetCampaign
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(32.dp))

                            Text(
                                "Quick Summary",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (data.characters.isNotEmpty()) {
                                        Text(
                                            "Recent Heroes: ${data.characters.take(3).joinToString { it.name }}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    if (data.npcs.isNotEmpty()) {
                                        Text(
                                            "Key NPCs: ${data.npcs.take(3).joinToString { it.name }}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                    val encodedId = com.dnd.helper.domain.common.IdUtils.encode(campaign.id)
                                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text(
                                            "Campaign ID for players: $encodedId",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(
                                                    androidx.compose.ui.text.AnnotatedString(encodedId)
                                                )
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.ContentCopy,
                                                contentDescription = "Copy Campaign ID",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
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

// --- Data class for transfer dialog state ---
private data class TransferDialogData(
    val character: Character,
    val sourceSessionId: String,
    val targetCampaign: Campaign
)

// --- Character row with drag support ---
@Composable
private fun CharacterRow(
    character: Character,
    otherCampaigns: List<Campaign>,
    isDragging: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onTransferClick: (Campaign) -> Unit
) {
    var showTransferMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDragging) {
                    Modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .pointerInput(character.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() },
                        onDrag = { change, _ -> change.consume() }
                    )
                }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DragIndicator, "Drag to move/copy", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(character.name, fontWeight = FontWeight.SemiBold)
                Text(
                    "Level ${character.level} ${character.race} ${character.characterClass}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Transfer/Copy button with dropdown menu
            Box {
                IconButton(onClick = { showTransferMenu = true }) {
                    Icon(Icons.Default.SwapHoriz, "Transfer or copy character")
                }
                DropdownMenu(
                    expanded = showTransferMenu,
                    onDismissRequest = { showTransferMenu = false }
                ) {
                    Text(
                        "Transfer/Copy to:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    if (otherCampaigns.isEmpty()) {
                        Text(
                            "No other campaigns",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    } else {
                        otherCampaigns.forEach { targetCampaign ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(targetCampaign.name)
                                    }
                                },
                                onClick = {
                                    showTransferMenu = false
                                    onTransferClick(targetCampaign)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Transfer / Copy Dialog ---
@Composable
private fun TransferCopyDialog(
    character: Character,
    sourceCampaignName: String,
    targetCampaign: Campaign,
    onDismiss: () -> Unit,
    onConfirm: (isCopy: Boolean, transferItems: Boolean) -> Unit
) {
    var isCopy by remember { mutableStateOf(false) }
    var transferItems by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SwapHoriz, null)
                Spacer(Modifier.width(8.dp))
                Text("Transfer Character")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "\"${character.name}\" from \"$sourceCampaignName\" to \"${targetCampaign.name}\"",
                    style = MaterialTheme.typography.bodyLarge
                )

                HorizontalDivider()

                // Move vs Copy
                Text("Action:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !isCopy,
                        onClick = { isCopy = false },
                        label = { Text("Move") },
                        leadingIcon = { Icon(Icons.Default.SwapHoriz, null, Modifier.size(18.dp)) }
                    )
                    FilterChip(
                        selected = isCopy,
                        onClick = { isCopy = true },
                        label = { Text("Copy") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp)) }
                    )
                }

                if (isCopy) {
                    Text(
                        "A new character copy will be created without player assignment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

                // Items transfer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Transfer items", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (transferItems) "${character.items.size} items included" else "No items",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = transferItems,
                        onCheckedChange = { transferItems = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(isCopy, transferItems) }) {
                Text(if (isCopy) "Copy" else "Move")
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
