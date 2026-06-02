package com.dnd.helper.presentation.desktop

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.dnd.helper.theme.LocalDndColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LogScreen(
    viewModel: LogViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                    val isCharacterAction = log.action.contains("Character", ignoreCase = true)
                    val hasJsonState = remember(log.initialState) {
                        log.initialState?.trim()?.startsWith("{") == true
                    }

                    if (hasJsonState && isCharacterAction) {
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

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    if (log.initialState != null && log.endState != null) {
                        Text("State Comparison:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        
                        // Calculate diff only when expanded and off the main thread
                        var diffLines by remember { mutableStateOf<List<String>>(emptyList()) }
                        var isCalculating by remember { mutableStateOf(true) }
                        
                        LaunchedEffect(log, expanded) {
                            if (expanded) {
                                isCalculating = true
                                diffLines = withContext(Dispatchers.Default) {
                                    calculateVisualDiff(log.initialState, log.endState)
                                }
                                isCalculating = false
                            }
                        }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                                .padding(12.dp)
                        ) {
                            if (isCalculating) {
                                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                            } else if (diffLines.isEmpty()) {
                                Text("No data changes detected in JSON", style = MaterialTheme.typography.bodySmall)
                            } else {
                                diffLines.forEach { line ->
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
                                        ),
                                        color = if (line.startsWith("-")) LocalDndColors.current.diffRemoved else if (line.startsWith("+")) LocalDndColors.current.diffAdded else MaterialTheme.colorScheme.onSurface
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
    if (initial.isNullOrBlank() || end.isNullOrBlank()) return emptyList()
    if (initial == end) return emptyList()
    
    val lines = mutableListOf<String>()
    
    try {
        val json = Json { prettyPrint = true; isLenient = true }
        
        val isInitialJson = initial.trim().let { it.startsWith("{") || it.startsWith("[") }
        val isEndJson = end.trim().let { it.startsWith("{") || it.startsWith("[") }

        if (!isInitialJson && !isEndJson && initial.length < 100 && end.length < 100) {
            return listOf("- $initial", "+ $end")
        }

        // Parse and re-format to ensure consistent pretty printing
        val initialPretty = try {
            if (isInitialJson) {
                val initialObj = Json.parseToJsonElement(initial)
                json.encodeToString(initialObj).lines()
            } else initial.lines()
        } catch (e: Exception) {
            initial.lines()
        }
        
        val endPretty = try {
            if (isEndJson) {
                val endObj = Json.parseToJsonElement(end)
                json.encodeToString(endObj).lines()
            } else end.lines()
        } catch (e: Exception) {
            end.lines()
        }
        
        // Use Sets for O(1) lookups to avoid O(N^2) complexity which hangs the UI
        val initialSet = initialPretty.map { it.trim() }.toSet()
        val endSet = endPretty.map { it.trim() }.toSet()
        
        // Find lines removed (in initial but not in end)
        initialPretty.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank() && trimmed != "{" && trimmed != "}" && trimmed != "[" && trimmed != "]" && trimmed !in endSet) {
                lines.add("- $line")
            }
            if (lines.size > 25) return@forEach 
        }
        
        // Find lines added (in end but not in initial)
        endPretty.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank() && trimmed != "{" && trimmed != "}" && trimmed != "[" && trimmed != "]" && trimmed !in initialSet) {
                lines.add("+ $line")
            }
            if (lines.size > 50) return@forEach
        }
        
        if (lines.isEmpty() && initialPretty != endPretty) {
            lines.add("Data changed (non-structural or whitespace)")
        }
        
    } catch (e: Exception) {
        lines.add("Error comparing: ${e.message}")
    }
    
    return lines
}
