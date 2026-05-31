package com.dnd.helper.presentation.characterdetail.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.presentation.characterdetail.CharacterDetailEvent

@Composable
fun InventoryTab(
    items: List<Item>,
    onEvent: (CharacterDetailEvent) -> Unit,
    isMasterMode: Boolean = false,
) {
    val equippedItems = remember(items) {
        items.filter { it.equipped }.associateBy { it.slot }
    }
    val inventoryItems = remember(items) {
        items.filter { !it.equipped }
    }

    var selectedItem by remember { mutableStateOf<Item?>(null) }

    selectedItem?.let { item ->
        ItemDetailDialog(
            item = item,
            onDismiss = { selectedItem = null },
            onToggleEquip = {
                onEvent(CharacterDetailEvent.ToggleItemEquipped(item.id))
                selectedItem = null
            },
            isMasterMode = isMasterMode,
            onDelete = {
                // TODO: onEvent(CharacterDetailEvent.DeleteItem(item.id))
                selectedItem = null
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top half — Equipment Panel
        EquipmentPanel(
            equippedItems = equippedItems,
            onSlotClick = { slot ->
                equippedItems[slot]?.let { selectedItem = it }
            },
            modifier = Modifier.fillMaxHeight(0.5f)
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )

        // Bottom half — Item Grid
        ItemGrid(
            items = inventoryItems,
            onItemClick = { selectedItem = it },
            modifier = Modifier.fillMaxHeight(1f),
            isMasterMode = isMasterMode,
            onAddItem = {
                // TODO: onEvent(CharacterDetailEvent.ShowAddItemDialog)
            }
        )
    }
}

@Composable
private fun EquipmentPanel(
    equippedItems: Map<EquipmentSlot?, Item>,
    onSlotClick: (EquipmentSlot) -> Unit,
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
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 4.dp),
            size = 64.dp
        )

        // Main Hand — center left
        EquipmentSlotBox(
            slot = EquipmentSlot.MAIN_HAND,
            item = equippedItems[EquipmentSlot.MAIN_HAND],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp),
            size = 72.dp
        )

        // Off Hand — center right
        EquipmentSlotBox(
            slot = EquipmentSlot.OFF_HAND,
            item = equippedItems[EquipmentSlot.OFF_HAND],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
            size = 72.dp
        )

        // Body — slightly below center
        EquipmentSlotBox(
            slot = EquipmentSlot.BODY,
            item = equippedItems[EquipmentSlot.BODY],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.Center).padding(top = 48.dp),
            size = 64.dp
        )

        // Hands — bottom left
        EquipmentSlotBox(
            slot = EquipmentSlot.HANDS,
            item = equippedItems[EquipmentSlot.HANDS],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 24.dp, bottom = 8.dp),
            size = 56.dp
        )

        // Feet — bottom center
        EquipmentSlotBox(
            slot = EquipmentSlot.FEET,
            item = equippedItems[EquipmentSlot.FEET],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
            size = 56.dp
        )

        // Ring — bottom right
        EquipmentSlotBox(
            slot = EquipmentSlot.RING,
            item = equippedItems[EquipmentSlot.RING],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 8.dp),
            size = 56.dp
        )

        // Amulet — top right
        EquipmentSlotBox(
            slot = EquipmentSlot.AMULET,
            item = equippedItems[EquipmentSlot.AMULET],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp, top = 12.dp),
            size = 56.dp
        )
    }
}

@Composable
private fun EquipmentSlotBox(
    slot: EquipmentSlot,
    item: Item?,
    onClick: (EquipmentSlot) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp
) {
    val rarityColor = item?.rarity?.toColor()
        ?: MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)

    Card(
        modifier = modifier
            .size(size)
            .clickable(enabled = item != null) { onClick(slot) },
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
                if (!item.displayImageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.displayImageUrl,
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    )
                } else {
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
    onItemClick: (Item) -> Unit,
    modifier: Modifier = Modifier,
    isMasterMode: Boolean = false,
    onAddItem: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Inventory (${items.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (isMasterMode) {
                IconButton(onClick = onAddItem) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }

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
                    ItemCell(item, onClick = { onItemClick(item) })
                }
            }
        }
    }
}

@Composable
private fun ItemCell(item: Item, onClick: () -> Unit) {
    val rarityColor = item.rarity.toColor()
    val icon = itemToIcon(item)

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = rarityColor.copy(alpha = 0.10f)
        ),
        border = BorderStroke(1.5.dp, rarityColor),
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (!item.displayImageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.displayImageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )
            } else {
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
    }
}

@Composable
private fun ItemDetailDialog(
    item: Item,
    onDismiss: () -> Unit,
    onToggleEquip: () -> Unit,
    isMasterMode: Boolean = false,
    onDelete: () -> Unit = {}
) {
    val rarityColor = item.rarity.toColor()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                // Item Image
                if (!item.displayImageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = item.displayImageUrl,
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = itemToIcon(item),
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = rarityColor,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = item.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall,
                                color = rarityColor,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Slot
                if (item.slot != null) {
                    DetailRow("Slot", item.slot.name.replace("_", " "))
                }

                // Equipped status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Equipped",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Checkbox(
                        checked = item.equipped,
                        onCheckedChange = { onToggleEquip() },
                    )
                }

                // Description
                if (item.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onToggleEquip,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (item.equipped) "Unequip" else "Equip")
                    }
                    
                    if (isMasterMode) {
                        Button(
                            onClick = onDelete,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Item")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
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
    item.name.contains("Scroll", ignoreCase = true) -> Icons.Default.Description
    item.slot != null -> slotToIcon(item.slot)
    else -> Icons.Default.Star
}
