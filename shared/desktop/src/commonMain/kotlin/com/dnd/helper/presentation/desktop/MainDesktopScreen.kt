package com.dnd.helper.presentation.desktop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.diceroll.DiceRollDialog
import com.dnd.helper.theme.DndIcons
import com.dnd.helper.theme.ThemeDialog
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

sealed class DesktopTab(val title: String, val icon: ImageVector) {
    data object Characters : DesktopTab("Characters", DndIcons.Filled.People)
    data object Library : DesktopTab("Library", DndIcons.Filled.LibraryBooks)
    data object RulesLibrary : DesktopTab("D&D Rules", DndIcons.Filled.MenuBook)
    data object Creator : DesktopTab("Creator", Icons.Default.AddCircle)
    data object Logs : DesktopTab("Logs", DndIcons.Filled.History)
    data object Presenter : DesktopTab("Presenter", DndIcons.Filled.Tv)
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

@Composable
fun MainDesktopScreen(
    onLogout: () -> Unit = {},
    onSwitchCampaign: () -> Unit = {},
    presentationViewModel: PresentationViewModel = koinViewModel()
) {
    var selectedTab by remember { mutableStateOf<DesktopTab>(DesktopTab.Characters) }
    var selectedCharacterId by remember { mutableStateOf<String?>(null) }
    var initialCreatorType by remember { mutableStateOf<CreatorType?>(null) }
    var showDiceDialog by remember { mutableStateOf(false) }
    var showMusicPlayer by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var musicPlayerOffset by remember { mutableStateOf(IntOffset(0, 0)) }

    val storage = org.koin.compose.koinInject<com.dnd.helper.domain.storage.CharacterStorage>()
    val activeTableId = storage.getTableId() ?: ""

    val isWindowOpen by presentationViewModel.isWindowOpen.collectAsState()
    val showStats by presentationViewModel.showStats.collectAsState()
    val activeItems = presentationViewModel.activeItems

    // Secondary Window for Players
    ExternalWindow(
        isOpen = isWindowOpen,
        onCloseRequest = { presentationViewModel.setWindowOpen(false) }
    ) {
        com.dnd.helper.theme.DndHelperTheme {
            PlayerViewContent(
                activeItems = activeItems, 
                showStats = showStats,
                onCloseRequest = { presentationViewModel.setWindowOpen(false) }
            )
        }
    }

    if (showDiceDialog) {
        DiceRollDialog(onDismiss = { showDiceDialog = false })
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
                            imageVector = if (showMusicPlayer) DndIcons.Filled.MusicNote else DndIcons.Filled.MusicOff,
                            contentDescription = "Music",
                            tint = if (showMusicPlayer) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Switch Campaign Button
                    FloatingActionButton(
                        onClick = onSwitchCampaign,
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(
                            imageVector = DndIcons.Filled.Storage,
                            contentDescription = "Switch Campaign",
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
                        Icon(
                            imageVector = DndIcons.Filled.Casino,
                            contentDescription = "Roll Dice"
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Theme Button
                    FloatingActionButton(
                        onClick = { showThemeDialog = true },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp)
                    ) {
                        Icon(DndIcons.Filled.Palette, "Theme")
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
                                    initialCreatorType = CreatorType.Character()
                                    selectedTab = DesktopTab.Creator 
                                },
                                onEditCharacter = { character ->
                                    initialCreatorType = CreatorType.Character(character)
                                    selectedTab = DesktopTab.Creator
                                },
                                sessionKey = activeTableId,
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
                                when (initialCreatorType) {
                                    is CreatorType.Character -> {
                                        selectedTab = DesktopTab.Characters
                                    }
                                    null -> {
                                        selectedTab = DesktopTab.Characters
                                    }
                                    else -> {
                                        selectedTab = DesktopTab.Library
                                    }
                                }
                                initialCreatorType = null
                            },
                            onBack = {
                                when (initialCreatorType) {
                                    is CreatorType.Character -> {
                                        selectedTab = DesktopTab.Characters
                                    }
                                    null -> {
                                        selectedTab = DesktopTab.Characters
                                    }
                                    else -> {
                                        selectedTab = DesktopTab.Library
                                    }
                                }
                                initialCreatorType = null
                            }
                        )
                        DesktopTab.Logs -> LogScreen()
                        DesktopTab.Presenter -> PresentationScreen()
                        DesktopTab.Settings -> SettingsScreen(onLogout = onLogout)
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
    onEditCharacter: (com.dnd.helper.domain.model.Character) -> Unit = {},
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
                    viewModel = viewModel,
                    onEditClick = onEditCharacter
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = DndIcons.Filled.Shield,
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
