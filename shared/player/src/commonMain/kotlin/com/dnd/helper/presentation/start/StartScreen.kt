package com.dnd.helper.presentation.start

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnd.helper.theme.DndIcons
import com.dnd.helper.theme.ThemeDialog
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    onLoadCharacter: (String) -> Unit,
    onLogout: () -> Unit = {},
    onCreateCharacter: () -> Unit = {},
    onEditCharacter: (String) -> Unit = {},
    viewModel: StartViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showIncomingAssignments by remember { mutableStateOf(false) }
    // (charId, gameId input) for join-campaign dialog
    var joinDialogCharId by remember { mutableStateOf<String?>(null) }
    var joinGameId by remember { mutableStateOf("") }

    val pullToRefreshState = rememberPullToRefreshState()

    val pendingCount = state.pendingAssignments.size

    if (showThemeDialog) {
        ThemeDialog(onDismiss = { showThemeDialog = false })
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirmation = false
                    viewModel.onEvent(StartEvent.Logout)
                    onLogout()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // === Incoming Assignments Dialog ===
    if (showIncomingAssignments) {
        IncomingAssignmentsDialog(
            assignments = state.pendingAssignments,
            isLoading = state.isLoadingAssignments,
            error = state.assignmentError,
            onAccept = { assignmentId ->
                viewModel.onEvent(StartEvent.RespondToAssignment(assignmentId, accept = true))
            },
            onRevoke = { assignmentId ->
                viewModel.onEvent(StartEvent.RespondToAssignment(assignmentId, accept = false))
            },
            onDismiss = { showIncomingAssignments = false }
        )
    }

    // === Join Campaign Dialog ===
    val activeCharId = joinDialogCharId
    if (activeCharId != null) {
        AlertDialog(
            onDismissRequest = {
                joinDialogCharId = null
                joinGameId = ""
            },
            title = { Text("Join Campaign") },
            text = {
                Column {
                    Text(
                        "Enter the Game ID shared by your DM:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = joinGameId,
                        onValueChange = { joinGameId = it },
                        label = { Text("Game ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    state.joinError?.let { err ->
                        Spacer(Modifier.height(4.dp))
                        Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.onEvent(StartEvent.JoinCampaign(activeCharId, joinGameId.trim()))
                        joinDialogCharId = null
                        joinGameId = ""
                    },
                    enabled = joinGameId.isNotBlank() && !state.isJoiningCampaign
                ) {
                    if (state.isJoiningCampaign) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Join")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    joinDialogCharId = null
                    joinGameId = ""
                }) { Text("Cancel") }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        PullToRefreshBox(
            isRefreshing = state.isLoadingMyCharacters,
            onRefresh = { viewModel.onEvent(StartEvent.RefreshMyCharacters) },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top-right actions
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Incoming Assignments button with badge (always visible)
                    Box {
                        IconButton(onClick = { showIncomingAssignments = true }) {
                            Icon(Icons.Default.Notifications, "Incoming assignments")
                        }
                        if (pendingCount > 0) {
                            Badge(
                                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                            ) {
                                Text(pendingCount.toString())
                            }
                        }
                    }
                    IconButton(onClick = { showThemeDialog = true }) {
                        Icon(DndIcons.Filled.Palette, "Theme")
                    }
                    IconButton(onClick = { showLogoutConfirmation = true }) {
                        Icon(DndIcons.Filled.Logout, "Logout")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = "D&D Helper",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // === My Characters Section ===
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Characters",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        OutlinedButton(
                            onClick = onCreateCharacter,
                            modifier = Modifier
                        ) {
                            Text("+ Create")
                        }
                    }

                    if (state.isLoadingMyCharacters && state.characterTemplates.isEmpty() && state.standaloneInstances.isEmpty()) {
                        CircularProgressIndicator()
                    } else if (state.characterTemplates.isEmpty() && state.standaloneInstances.isEmpty() && !state.isLoadingMyCharacters) {
                        Text(
                            text = "No characters yet.\nTap \"+ Create\" to make one, or wait for your DM to assign one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.characterTemplates) { templateDto ->
                                CharacterTemplateCard(
                                    templateDto = templateDto,
                                    onEditTemplate = { onEditCharacter(templateDto.template.id) },
                                    onPlayInstance = { instance ->
                                        viewModel.onEvent(
                                            StartEvent.LoadMyCharacter(instance.character.id, instance.sessionId)
                                        )
                                        onLoadCharacter(instance.character.id)
                                    },
                                    onJoinCampaign = {
                                        joinDialogCharId = templateDto.template.id
                                        joinGameId = ""
                                        viewModel.onEvent(StartEvent.DismissJoinError)
                                    },
                                    onDelete = {
                                        viewModel.onEvent(StartEvent.DeleteCharacter(templateDto.template.id))
                                    }
                                )
                            }

                            if (state.standaloneInstances.isNotEmpty()) {
                                item {
                                    Text(
                                        "In campaigns",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                items(state.standaloneInstances) { instance ->
                                    StandaloneInstanceCard(
                                        instance = instance,
                                        onPlay = {
                                            viewModel.onEvent(
                                                StartEvent.LoadMyCharacter(instance.character.id, instance.sessionId)
                                            )
                                            onLoadCharacter(instance.character.id)
                                        }
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

@Composable
fun CharacterTemplateCard(
    templateDto: com.dnd.helper.data.remote.dto.auth.CharacterTemplateDto,
    onEditTemplate: () -> Unit,
    onPlayInstance: (com.dnd.helper.data.remote.dto.auth.MyCharacterDto) -> Unit,
    onJoinCampaign: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(0f) }
    var cardWidthPx by remember { mutableStateOf(0) }
    val template = templateDto.template

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Character") },
            text = {
                Text(
                    "Are you sure you want to delete the template \"${template.name}\"? Your copies in active campaigns will remain."
                )
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { cardWidthPx = it.size.width }
            .pointerInput(template.id) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        val threshold = cardWidthPx * 0.8f
                        if (cardWidthPx > 0 && dragOffset <= -threshold) {
                            showDeleteDialog = true
                        }
                        dragOffset = 0f
                    }
                ) { _, dragAmount ->
                    val limit = if (cardWidthPx > 0) cardWidthPx.toFloat() else 400f
                    dragOffset = (dragOffset + dragAmount).coerceIn(-limit, 0f)
                }
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { androidx.compose.ui.unit.IntOffset(dragOffset.roundToInt(), 0) },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = template.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${template.race} ${template.characterClass}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) DndIcons.Filled.ExpandLess else DndIcons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand"
                        )
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onEditTemplate,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit Template")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Campaigns",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    if (templateDto.instances.isEmpty()) {
                        Text(
                            "Not in any campaigns yet.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            templateDto.instances.forEach { instance ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    ),
                                    onClick = { onPlayInstance(instance) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                instance.campaignName ?: "Unknown Campaign",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                "Level ${instance.character.level}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onJoinCampaign,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Join New Campaign")
                    }
                } else {
                    if (templateDto.instances.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "In ${templateDto.instances.size} campaigns",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StandaloneInstanceCard(
    instance: com.dnd.helper.data.remote.dto.auth.MyCharacterDto,
    onPlay: () -> Unit
) {
    val character = instance.character
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = instance.campaignName ?: "Unknown Campaign",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${character.race} ${character.characterClass} • Level ${character.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IncomingAssignmentsDialog(
    assignments: List<com.dnd.helper.data.remote.dto.auth.PendingAssignmentDto>,
    isLoading: Boolean,
    error: String?,
    onAccept: (String) -> Unit,
    onRevoke: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null)
                Spacer(Modifier.size(8.dp))
                Text("Incoming Character Assignments")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }

                if (assignments.isEmpty() && !isLoading) {
                    Text(
                        "No pending assignments.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                assignments.forEach { assignment ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = assignment.character.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Level ${assignment.character.level} ${assignment.character.race} ${assignment.character.characterClass}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            assignment.campaignName?.let { campaign ->
                                Text(
                                    text = "Campaign: $campaign",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            assignment.masterUsername?.let { master ->
                                Text(
                                    text = "From: $master",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onRevoke(assignment.assignmentId) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Revoke", maxLines = 1)
                                }
                                Button(
                                    onClick = { onAccept(assignment.assignmentId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Accept", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
