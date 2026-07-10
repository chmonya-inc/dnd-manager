package com.dnd.helper.presentation.start

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnd.helper.theme.DndIcons
import com.dnd.helper.theme.ThemeDialog
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    onLoadCharacter: (String) -> Unit,
    onLogout: () -> Unit = {},
    viewModel: StartViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showIncomingAssignments by remember { mutableStateOf(false) }

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
                    Text(
                        text = "My Characters",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (state.isLoadingMyCharacters && state.myCharacters.isEmpty()) {
                        CircularProgressIndicator()
                    } else if (state.myCharacters.isEmpty() && !state.isLoadingMyCharacters) {
                        Text(
                            text = "No characters assigned to you yet.\nAsk your DM to assign one!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.myCharacters) { myChar ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        viewModel.onEvent(StartEvent.LoadMyCharacter(myChar.character.id))
                                        onLoadCharacter(myChar.character.id)
                                    }
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = myChar.character.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Level ${myChar.character.level} ${myChar.character.race} ${myChar.character.characterClass}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        myChar.campaignName?.let { campaign ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Campaign: $campaign",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
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
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Revoke")
                                }
                                Button(
                                    onClick = { onAccept(assignment.assignmentId) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Accept")
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
