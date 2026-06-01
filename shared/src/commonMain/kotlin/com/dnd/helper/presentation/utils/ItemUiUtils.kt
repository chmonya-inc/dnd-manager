package com.dnd.helper.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity

fun ItemRarity.toColor(): Color = when (this) {
    ItemRarity.COMMON -> Color(0xFF9E9E9E)
    ItemRarity.UNCOMMON -> Color(0xFF43A047)
    ItemRarity.RARE -> Color(0xFF1E88E5)
    ItemRarity.EPIC -> Color(0xFF8E24AA)
    ItemRarity.LEGENDARY -> Color(0xFFFB8C00)
}

fun slotToIcon(slot: EquipmentSlot): ImageVector = when (slot) {
    EquipmentSlot.HEAD -> Icons.Default.Face
    EquipmentSlot.BODY -> Icons.Default.HealthAndSafety
    EquipmentSlot.HANDS -> Icons.Default.SportsMartialArts
    EquipmentSlot.FEET -> Icons.Default.Explore
    EquipmentSlot.MAIN_HAND -> Icons.Default.Bolt
    EquipmentSlot.OFF_HAND -> Icons.Default.Shield
    EquipmentSlot.RING -> Icons.Default.Star
    EquipmentSlot.AMULET -> Icons.Default.Favorite
}

fun itemToIcon(item: Item): ImageVector = when {
    item.name.contains("Potion", ignoreCase = true) -> Icons.Default.FavoriteBorder
    item.name.contains("Scroll", ignoreCase = true) -> Icons.Default.Description
    item.slot != null -> slotToIcon(item.slot)
    else -> Icons.Default.ShoppingBag
}
