package com.dnd.helper.presentation.desktop

import androidx.compose.ui.graphics.vector.ImageVector
import com.dnd.helper.theme.DndIcons

enum class LibraryType(val title: String, val icon: ImageVector) {
    Items("Items", DndIcons.Filled.Inventory),
    Mobs("Monsters", DndIcons.Filled.BugReport),
    Npcs("NPCs", DndIcons.Filled.EmojiPeople),
    Locations("Locations", DndIcons.Filled.Map),
    Battlefields("Battlefields", DndIcons.Filled.Map),
    Templates("Templates", DndIcons.Filled.AutoAwesome)
}
