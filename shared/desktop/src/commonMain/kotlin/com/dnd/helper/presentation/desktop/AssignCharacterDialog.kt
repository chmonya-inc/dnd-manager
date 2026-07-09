package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignCharacterDialog(
    characterId: String,
    sessionId: String,
    onDismiss: () -> Unit,
    viewModel: AssignCharacterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadAssignmentStatuses(sessionId)
    }

    AlertDialog(
        onDismissRequest = { if (!state.isAssigning) onDismiss() },
        title = { Text("Assign Character") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Enter the player's username to send an assignment request:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = state.username,
                    onValueChange = { viewModel.onUsernameChanged(it) },
                    label = { Text("Player Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isAssigning,
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isAssigning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Sending request...")
                    }
                }

                if (state.success) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("Request sent! Waiting for player response.", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                    }
                }

                state.error?.let { error ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(text = error, color = MaterialTheme.colorScheme.error)
                    }
                }

                // Assignment status history for this session
                if (state.assignmentStatuses.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Recent Assignments",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.assignmentStatuses.take(10)) { status ->
                            AssignmentStatusRow(status)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { viewModel.unassignCharacter(characterId, sessionId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isAssigning,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.PersonOff, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Unassign Character")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.assignCharacter(characterId, sessionId) },
                enabled = !state.isAssigning && state.username.isNotBlank(),
            ) {
                Text("Send Request")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !state.isAssigning
            ) {
                Text(if (state.success) "Close" else "Cancel")
            }
        }
    )
}

@Composable
private fun AssignmentStatusRow(status: com.dnd.helper.data.remote.dto.auth.AssignmentStatusDto) {
    val statusInfo = when (status.status) {
        "PENDING" -> Triple(Icons.Default.HourglassEmpty, "Pending", MaterialTheme.colorScheme.secondary)
        "ACCEPTED" -> Triple(Icons.Default.CheckCircle, "Accepted", Color(0xFF4CAF50))
        "REVOKED" -> Triple(Icons.Default.PersonOff, "Revoked", MaterialTheme.colorScheme.error)
        else -> Triple(Icons.Default.Person, status.status, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(statusInfo.first, null, modifier = Modifier.size(18.dp), tint = statusInfo.third)
        Column(modifier = Modifier.weight(1f)) {
            Text(status.characterName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            status.playerUsername?.let { Text("→ $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Text(statusInfo.second, style = MaterialTheme.typography.labelSmall, color = statusInfo.third, fontWeight = FontWeight.Bold)
    }
}
