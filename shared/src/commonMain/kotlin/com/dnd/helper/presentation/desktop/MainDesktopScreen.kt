package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dnd.helper.presentation.characterdetail.CharacterDetailScreen
import com.dnd.helper.presentation.characterdetail.CharacterDetailViewModel
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

sealed class DesktopTab(val title: String, val icon: ImageVector) {
    data object Characters : DesktopTab("Characters", Icons.Default.People)
    data object Library : DesktopTab("Library", Icons.AutoMirrored.Filled.LibraryBooks)
    data object Creator : DesktopTab("Creator", Icons.Default.AddCircle)
    data object Presenter : DesktopTab("Presenter", Icons.Default.Tv)
}

private val tabs = listOf(
    DesktopTab.Characters,
    DesktopTab.Library,
    DesktopTab.Creator,
    DesktopTab.Presenter
)

@Composable
fun MainDesktopScreen() {
    var selectedTab by remember { mutableStateOf<DesktopTab>(DesktopTab.Characters) }
    var selectedCharacterId by remember { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail
        NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Spacer(Modifier.weight(1f))
            tabs.forEach { tab ->
                NavigationRailItem(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    icon = { Icon(tab.icon, contentDescription = tab.title) },
                    label = { Text(tab.title) }
                )
            }
            Spacer(Modifier.weight(1f))
        }

        // Content Area
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                DesktopTab.Characters -> {
                    CharactersSplitPane(
                        selectedCharacterId = selectedCharacterId,
                        onCharacterSelected = { selectedCharacterId = it },
                        onCreateCharacter = { selectedTab = DesktopTab.Creator }
                    )
                }
                DesktopTab.Library -> LibraryScreen()
                DesktopTab.Creator -> CreatorScreen()
                DesktopTab.Presenter -> PresentationScreen()
            }
        }
    }
}

@Composable
fun CharactersSplitPane(
    selectedCharacterId: String?,
    onCharacterSelected: (String) -> Unit,
    onCreateCharacter: () -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        // Left Panel: Character List (30%)
        Box(modifier = Modifier.fillMaxWidth(0.3f).fillMaxHeight()) {
            CharacterListScreen(
                onCharacterClick = onCharacterSelected,
                onCreateCharacter = onCreateCharacter,
                showTopBar = false
            )
        }

        // Vertical Divider
        VerticalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Right Panel: Character Detail (70%)
        Box(modifier = Modifier.fillMaxSize()) {
            if (selectedCharacterId != null) {
                val viewModel: CharacterDetailViewModel = koinViewModel(key = selectedCharacterId) {
                    parametersOf(selectedCharacterId)
                }
                CharacterDetailScreen(
                    viewModel = viewModel,
                    onBackClick = { /* No-op in desktop split pane */ }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Select a character to view details", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            Text("Coming Soon", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
