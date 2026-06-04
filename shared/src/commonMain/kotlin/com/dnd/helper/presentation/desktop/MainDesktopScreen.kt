package com.dnd.helper.presentation.desktop

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dnd.helper.data.import.SessionImporter
import com.dnd.helper.domain.common.IdUtils
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.storage.CharacterStorage
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.diceroll.DiceRollDialog
import com.dnd.helper.theme.AppTheme
import com.dnd.helper.theme.ThemeViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

sealed class DesktopTab(val title: String, val icon: ImageVector) {
    data object Characters : DesktopTab("Characters", Icons.Default.People)
    data object Library : DesktopTab("Library", Icons.AutoMirrored.Filled.LibraryBooks)
    data object RulesLibrary : DesktopTab("D&D Rules", Icons.Default.MenuBook)
    data object Creator : DesktopTab("Creator", Icons.Default.AddCircle)
    data object Logs : DesktopTab("Logs", Icons.Default.History)
    data object Presenter : DesktopTab("Presenter", Icons.Default.Tv)
    data object Settings : DesktopTab("Settings", Icons.Default.Settings)
}

private val primaryTabs = listOf(
    DesktopTab.Characters,
    DesktopTab.Library,
    DesktopTab.RulesLibrary,
    DesktopTab.Creator,
    DesktopTab.Presenter,
)

private val secondaryTabs = listOf(
    DesktopTab.Logs,
    DesktopTab.Settings,
)

private val tabs = primaryTabs + secondaryTabs

@Composable
fun MainDesktopScreen() {
    var selectedTab by remember { mutableStateOf<DesktopTab>(DesktopTab.Characters) }
    var selectedCharacterId by remember { mutableStateOf<String?>(null) }
    var initialCreatorType by remember { mutableStateOf<CreatorType?>(null) }
    var showDiceDialog by remember { mutableStateOf(false) }
    var showSessionsDialog by remember { mutableStateOf(false) }
    var showMusicPlayer by remember { mutableStateOf(false) }
    var showNeuralNetwork by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var musicPlayerOffset by remember { mutableStateOf(IntOffset(0, 0)) }

    // Track active session for forcing ViewModel recreation on session switch
    var activeTableId by remember { mutableStateOf<String?>(null) }

    if (showDiceDialog) {
        DiceRollDialog(onDismiss = { showDiceDialog = false })
    }

    if (showSessionsDialog) {
        SessionsDialog(
            onDismiss = { showSessionsDialog = false },
            onSessionSelected = { newTableId ->
                activeTableId = newTableId
                selectedCharacterId = null
                selectedTab = DesktopTab.Characters
                showSessionsDialog = false
            }
        )
    }

    if (showThemeDialog) {
        ThemeDialog(onDismiss = { showThemeDialog = false })
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.isCtrlPressed) {
                    when (keyEvent.key) {
                        Key.One -> { selectedTab = DesktopTab.Characters; true }
                        Key.Two -> { selectedTab = DesktopTab.Library; true }
                        Key.Three -> { selectedTab = DesktopTab.Creator; true }
                        Key.Four -> { selectedTab = DesktopTab.Presenter; true }
                        Key.Five -> { selectedTab = DesktopTab.Logs; true }
                        Key.Six -> { selectedTab = DesktopTab.Settings; true }
                        Key.M -> { showMusicPlayer = !showMusicPlayer; true }
                        Key.D -> { showDiceDialog = !showDiceDialog; true }
                        else -> false
                    }
                } else {
                    false
                }
            },
        color = MaterialTheme.colorScheme.background
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Navigation Rail
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                header = {
                    Spacer(Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(1f))
                    
                    // Primary tabs
                    primaryTabs.forEach { tab ->
                        NavigationRailItem(
                            selected = selectedTab == tab,
                            onClick = { 
                                selectedTab = tab
                                if (tab != DesktopTab.Creator) initialCreatorType = null 
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
                        )
                    }

                    // Divider between primary and secondary tabs
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .width(48.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    )

                    // Secondary tabs
                    secondaryTabs.forEach { tab ->
                        NavigationRailItem(
                            selected = selectedTab == tab,
                            onClick = { 
                                selectedTab = tab
                                if (tab != DesktopTab.Creator) initialCreatorType = null 
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) }
                        )
                    }
                    
                    Spacer(Modifier.weight(1f))

                    // Music Player Button
                    FloatingActionButton(
                        onClick = { showMusicPlayer = !showMusicPlayer },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (showMusicPlayer) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(
                            imageVector = if (showMusicPlayer) Icons.Default.MusicNote else Icons.Default.MusicOff,
                            contentDescription = "Music",
                            tint = if (showMusicPlayer) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Neural Network Button
                    FloatingActionButton(
                        onClick = { showNeuralNetwork = !showNeuralNetwork },
                        modifier = Modifier.size(48.dp),
                        containerColor = if (showNeuralNetwork) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Neural Network",
                            tint = if (showNeuralNetwork) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Sessions Button
                    FloatingActionButton(
                        onClick = { showSessionsDialog = true },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Sessions",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Global Dice Button on the left side
                    FloatingActionButton(
                        onClick = { showDiceDialog = true },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Text(text = "🎲", style = MaterialTheme.typography.titleLarge)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Theme Button
                    FloatingActionButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(Icons.Default.Palette, "Theme")
                    }
                }
            }

            // Content Area
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                ) { tab ->
                    when (tab) {
                        DesktopTab.Characters -> {
                            CharactersSplitPane(
                                selectedCharacterId = selectedCharacterId,
                                onCharacterSelected = { selectedCharacterId = it },
                                onCreateCharacter = { 
                                    initialCreatorType = CreatorType.Character
                                    selectedTab = DesktopTab.Creator 
                                },
                                sessionKey = activeTableId ?: "",
                            )
                        }
                        DesktopTab.Library -> LibraryScreen(
                            onNavigateToCreator = { type ->
                                initialCreatorType = type
                                selectedTab = DesktopTab.Creator
                            }
                        )
                        DesktopTab.RulesLibrary -> RulesLibraryScreen()
                        DesktopTab.Creator -> CreatorScreen(
                            initialType = initialCreatorType,
                            onCreated = {
                                if (initialCreatorType != null) {
                                    selectedTab = DesktopTab.Library
                                } else {
                                    selectedTab = DesktopTab.Characters
                                }
                                initialCreatorType = null
                            },
                            onBack = {
                                if (initialCreatorType != null) {
                                    selectedTab = DesktopTab.Library
                                } else {
                                    selectedTab = DesktopTab.Characters
                                }
                                initialCreatorType = null
                            }
                        )
                        DesktopTab.Logs -> LogScreen()
                        DesktopTab.Presenter -> PresentationScreen()
                        DesktopTab.Settings -> SettingsScreen()
                    }
                }

                if (showMusicPlayer) {
                    MusicPlayerScreen(
                        modifier = Modifier
                            .offset { musicPlayerOffset }
                            .padding(16.dp)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    musicPlayerOffset = IntOffset(
                                        x = (musicPlayerOffset.x + dragAmount.x).roundToInt().coerceIn(-1000, 1000),
                                        y = (musicPlayerOffset.y + dragAmount.y).roundToInt().coerceIn(-500, 500)
                                    )
                                }
                            },
                        onClose = { showMusicPlayer = false }
                    )
                }
            }
        }
    }
}

@Composable
fun CharactersSplitPane(
    selectedCharacterId: String?,
    onCharacterSelected: (String) -> Unit,
    onCreateCharacter: () -> Unit,
    sessionKey: String = "",
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Panel: Character List (30%)
        Box(modifier = Modifier.fillMaxWidth(0.3f).fillMaxHeight()) {
            CharacterListScreen(
                onCharacterClick = onCharacterSelected,
                onCreateCharacter = onCreateCharacter,
                showTopBar = true,
                showSummary = true,
                sessionKey = sessionKey,
            )
        }

        // Vertical Divider
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Right Panel: Character Detail (70%)
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedCharacterId != null) {
                val viewModel: com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel = koinViewModel(key = selectedCharacterId) {
                    parametersOf(selectedCharacterId)
                }
                MasterCharacterDetailScreen(
                    viewModel = viewModel
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Select a character",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Choose a character from the list to view their sheet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionsDialog(
    onDismiss: () -> Unit,
    onSessionSelected: (String) -> Unit,
    viewModel: SessionsViewModel = koinViewModel(),
) {
    val clipboardManager = LocalClipboardManager.current
    val state by viewModel.state.collectAsState()
    
    var newName by remember { mutableStateOf("") }
    var newId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sessions") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Active session info
                if (state.activeTableId.isNotBlank()) {
                    val activeSession = state.sessions.find { it.id == state.activeTableId }
                    Text(
                        text = "Active: ${activeSession?.name ?: "Unknown"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Saved sessions list
                if (state.sessions.isNotEmpty()) {
                    Text(
                        text = "Saved Sessions",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.sessions, key = { it.id }) { session ->
                            SessionRow(
                                session = session,
                                isActive = session.id == state.activeTableId,
                                onSelect = {
                                    viewModel.selectSession(session.id)
                                    onSessionSelected(session.id)
                                },
                                onDelete = {
                                    viewModel.deleteSession(session.id)
                                },
                                onCopy = {
                                    val encoded = IdUtils.encode(session.id)
                                    clipboardManager.setText(AnnotatedString(encoded))
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                } else {
                    Text(
                        text = "No saved sessions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                }

                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // Add new session
                Text(
                    text = "Add New Session",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Session Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newId,
                    onValueChange = { newId = it },
                    label = { Text("Join Existing Session ID (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.addSession(newName, newId)
                            newName = ""
                            newId = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newName.isNotBlank(),
                ) {
                    Text(if (newId.isNotBlank()) "Join Session" else "Create Session")
                }

                if (state.activeTableId.isNotBlank()) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Import Data to Active Session",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            viewModel.importData()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isImporting
                    ) {
                        if (state.isImporting) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text("Importing...")
                        } else {
                            Icon(Icons.Default.UploadFile, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Import from XLSX")
                        }
                    }
                    state.importError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = null,
    )
}

@Composable
private fun SessionRow(
    session: Session,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Ready to share",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Game ID",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                TextButton(
                    onClick = onSelect,
                    enabled = !isActive,
                ) {
                    Text(if (isActive) "Active" else "Select")
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeDialog(
    onDismiss: () -> Unit,
    viewModel: ThemeViewModel = koinViewModel()
) {
    val currentTheme by viewModel.currentTheme.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select UI Theme") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    Surface(
                        onClick = { viewModel.setTheme(theme) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        color = if (currentTheme == theme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (currentTheme == theme) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (currentTheme == theme) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                null,
                                tint = if (currentTheme == theme) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(theme.displayName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
