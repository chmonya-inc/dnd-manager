package com.dnd.helper.presentation.characterdetail.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnd.helper.domain.model.*

@Composable
fun InventoryTab(items: List<Item>) {
    val equippedItems = remember(items) {
        items.filter { it.equipped }.associateBy { it.slot }
    }
    val inventoryItems = remember(items) {
        items.filter { !it.equipped }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top half — Equipment Panel
        EquipmentPanel(
            equippedItems = equippedItems,
            modifier = Modifier.weight(1f)
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        // Bottom half — Item Grid
        ItemGrid(
            items = inventoryItems,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EquipmentPanel(
    equippedItems: Map<EquipmentSlot?, Item>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Character silhouette in center
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }

        // Head — top center
        EquipmentSlotBox(
            slot = EquipmentSlot.HEAD,
            item = equippedItems[EquipmentSlot.HEAD],
            modifier = Modifier.align(Alignment.TopCenter).offset(y = 4.dp),
            size = 64.dp
        )

        // Main Hand — center left
        EquipmentSlotBox(
            slot = EquipmentSlot.MAIN_HAND,
            item = equippedItems[EquipmentSlot.MAIN_HAND],
            modifier = Modifier.align(Alignment.CenterStart).offset(x = 8.dp),
            size = 72.dp
        )

        // Off Hand — center right
        EquipmentSlotBox(
            slot = EquipmentSlot.OFF_HAND,
            item = equippedItems[EquipmentSlot.OFF_HAND],
            modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-8).dp),
            size = 72.dp
        )

        // Body — slightly below center
        EquipmentSlotBox(
            slot = EquipmentSlot.BODY,
            item = equippedItems[EquipmentSlot.BODY],
            modifier = Modifier.align(Alignment.Center).offset(y = 48.dp),
            size = 64.dp
        )

        // Hands — bottom left
        EquipmentSlotBox(
            slot = EquipmentSlot.HANDS,
            item = equippedItems[EquipmentSlot.HANDS],
            modifier = Modifier.align(Alignment.BottomStart).offset(x = 24.dp, y = (-8).dp),
            size = 56.dp
        )

        // Feet — bottom center
        EquipmentSlotBox(
            slot = EquipmentSlot.FEET,
            item = equippedItems[EquipmentSlot.FEET],
            modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp),
            size = 56.dp
        )

        // Ring — bottom right
        EquipmentSlotBox(
            slot = EquipmentSlot.RING,
            item = equippedItems[EquipmentSlot.RING],
            modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-24).dp, y = (-8).dp),
            size = 56.dp
        )

        // Amulet — top right
        EquipmentSlotBox(
            slot = EquipmentSlot.AMULET,
            item = equippedItems[EquipmentSlot.AMULET],
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = 12.dp),
            size = 56.dp
        )
    }
}

@Composable
private fun EquipmentSlotBox(
    slot: EquipmentSlot,
    item: Item?,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val rarityColor = item?.rarity?.toColor()
        ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    Card(
        modifier = modifier.size(size),
        colors = CardDefaults.cardColors(
            containerColor = if (item != null)
                rarityColor.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        ),
        border = BorderStroke(
            width = if (item != null) 2.dp else 1.5.dp,
            color = rarityColor
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (item != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = slotToIcon(slot),
                        contentDescription = null,
                        modifier = Modifier.size(size * 0.32f),
                        tint = rarityColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = abbreviateName(item.name),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = (size.value * 0.11f).coerceAtLeast(8f).sp,
                        color = rarityColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            } else {
                Icon(
                    imageVector = slotToIcon(slot),
                    contentDescription = slot.name,
                    modifier = Modifier.size(size * 0.4f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                )
            }
        }
    }
}

@Composable
private fun ItemGrid(
    items: List<Item>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Inventory (${items.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No items in inventory",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 76.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ItemCell(item)
                }
            }
        }
    }
}

@Composable
private fun ItemCell(item: Item) {
    val rarityColor = item.rarity.toColor()
    val icon = itemToIcon(item)

    Card(
        modifier = Modifier.aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = rarityColor.copy(alpha = 0.10f)
        ),
        border = BorderStroke(1.5.dp, rarityColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = rarityColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelSmall,
                color = rarityColor,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun abbreviateName(name: String): String {
    return if (name.length <= 9) name else name.take(8) + "…"
}

private fun ItemRarity.toColor(): Color = when (this) {
    ItemRarity.COMMON -> Color(0xFF9E9E9E)
    ItemRarity.UNCOMMON -> Color(0xFF43A047)
    ItemRarity.RARE -> Color(0xFF1E88E5)
    ItemRarity.EPIC -> Color(0xFF8E24AA)
    ItemRarity.LEGENDARY -> Color(0xFFFB8C00)
}

private fun slotToIcon(slot: EquipmentSlot): ImageVector = when (slot) {
    EquipmentSlot.HEAD -> Icons.Default.Face
    EquipmentSlot.BODY -> Icons.Default.HealthAndSafety
    EquipmentSlot.HANDS -> Icons.Default.SportsMartialArts
    EquipmentSlot.FEET -> Icons.Default.Explore
    EquipmentSlot.MAIN_HAND -> Icons.Default.Bolt
    EquipmentSlot.OFF_HAND -> Icons.Default.Shield
    EquipmentSlot.RING -> Icons.Default.Star
    EquipmentSlot.AMULET -> Icons.Default.Favorite
}

private fun itemToIcon(item: Item): ImageVector = when {
    item.name.contains("Potion", ignoreCase = true) -> Icons.Default.FavoriteBorder
    item.name.contains("Scroll", ignoreCase = true) -> Icons.Default.AutoFixHigh
    item.slot != null -> slotToIcon(item.slot)
    else -> Icons.Default.Star
}
