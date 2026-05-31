package com.dnd.helper.presentation.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnd.helper.domain.model.LogEntry
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LogScreen(
    viewModel: LogViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(viewModel) {
        viewModel.startPolling()
        onDispose { viewModel.stopPolling() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Text("Activity Logs", style = MaterialTheme.typography.headlineMedium)
            }
            IconButton(onClick = { viewModel.refreshLogs(force = true) }) {
                Icon(Icons.Default.Refresh, "Refresh")
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No logs found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.logs) { log ->
                    LogCard(log, onUndo = { viewModel.undoLog(log) })
                }
            }
        }
    }
}

@Composable
fun LogCard(log: LogEntry, onUndo: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.action,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (log.success) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                    if (!log.details.isNullOrBlank()) {
                        Text(
                            text = log.details,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    Text(
                        text = (log.timestamp ?: "").split("T").joinToString(" ").split(".").first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.initialState != null && log.action.contains("Character")) {
                        Button(
                            onClick = { 
                                onUndo()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(Icons.Default.Undo, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Undo", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    if (log.initialState != null && log.endState != null) {
                        Text("State Comparison:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        
                        // Show raw diff or structured view
                        val diffLines = remember(log) { calculateVisualDiff(log.initialState, log.endState) }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            if (diffLines.isEmpty()) {
                                Text("No data changes detected in JSON", style = MaterialTheme.typography.bodySmall)
                            } else {
                                diffLines.forEach { line ->
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        ),
                                        color = if (line.startsWith("-")) Color(0xFFD32F2F) else if (line.startsWith("+")) Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No state data available for this log entry.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

private fun calculateVisualDiff(initial: String?, end: String?): List<String> {
    if (initial == null || end == null) return emptyList()
    
    // We try to find actual property changes in the JSON
    // A full JSON diff is complex, but we can do a simplified line-by-line or key-by-key comparison
    val lines = mutableListOf<String>()
    
    try {
        val json = Json { prettyPrint = true }
        val initialObj = Json.parseToJsonElement(initial)
        val endObj = Json.parseToJsonElement(end)
        
        val initialPretty = json.encodeToString(initialObj).lines()
        val endPretty = json.encodeToString(endObj).lines()
        
        // Simple line diff for visual representation
        var i = 0
        var j = 0
        while (i < initialPretty.size || j < endPretty.size) {
            val lineI = initialPretty.getOrNull(i)
            val lineJ = endPretty.getOrNull(j)
            
            if (lineI == lineJ) {
                // Keep some context lines around changes if needed, 
                // but for now only show changes to keep it "merged into line"
                i++; j++
            } else {
                if (lineI != null && (lineJ == null || !endPretty.contains(lineI))) {
                    lines.add("- $lineI")
                    i++
                }
                if (lineJ != null && (lineI == null || !initialPretty.contains(lineJ))) {
                    lines.add("+ $lineJ")
                    j++
                }
            }
            
            if (lines.size > 50) { // Safety limit
                lines.add("... diff truncated")
                break
            }
        }
    } catch (e: Exception) {
        lines.add("Error parsing states: ${e.message}")
    }
    
    return lines
}
