package com.dnd.helper.presentation.desktop

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class LibraryType(val title: String, val icon: ImageVector) {
    Items("Items", Icons.Default.Inventory),
    Mobs("Monsters", Icons.Default.BugReport),
    Npcs("NPCs", Icons.Default.EmojiPeople),
    Locations("Locations", Icons.Default.Map)
}
