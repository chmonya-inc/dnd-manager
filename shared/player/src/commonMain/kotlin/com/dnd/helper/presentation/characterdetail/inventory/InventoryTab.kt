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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.presentation.characterdetail.CharacterDetailEvent
import com.dnd.helper.presentation.utils.itemToIcon
import com.dnd.helper.presentation.utils.slotToIcon
import com.dnd.helper.presentation.utils.toColor
import com.dnd.helper.theme.DndIcons

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
        // Find the latest version of this item from the list to keep dialog in sync
        val latestItem = items.find { it.id == item.id } ?: item

        ItemDetailDialog(
            item = latestItem,
            onDismiss = { selectedItem = null },
            onToggleEquip = {
                onEvent(CharacterDetailEvent.ToggleItemEquipped(item.id))
                selectedItem = null
            },
            isMasterMode = isMasterMode,
            onDelete = {
                onEvent(CharacterDetailEvent.RemoveItem(item.id))
                selectedItem = null
            },
            onUpdate = { updatedItem ->
                onEvent(CharacterDetailEvent.UpdateItem(updatedItem))
            },
            onEvent = onEvent
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
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

        ItemGrid(
            items = inventoryItems,
            onItemClick = { selectedItem = it },
            modifier = Modifier.fillMaxHeight(1f),
            isMasterMode = true,
            onAddItem = {
                onEvent(CharacterDetailEvent.AddItem)
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
            .height(340.dp)
            .padding(16.dp)
    ) {
        // Character Background Silhouette
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }

        // --- Center Column ---
        EquipmentSlotBox(
            slot = EquipmentSlot.HEAD,
            item = equippedItems[EquipmentSlot.HEAD],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.TopCenter),
            size = 64.dp
        )

        EquipmentSlotBox(
            slot = EquipmentSlot.BODY,
            item = equippedItems[EquipmentSlot.BODY],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.Center),
            size = 76.dp
        )

        EquipmentSlotBox(
            slot = EquipmentSlot.FEET,
            item = equippedItems[EquipmentSlot.FEET],
            onClick = onSlotClick,
            modifier = Modifier.align(Alignment.BottomCenter),
            size = 64.dp
        )

        // --- Left Column ---
        Column(
            modifier = Modifier.align(Alignment.CenterStart).padding(start = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            EquipmentSlotBox(
                slot = EquipmentSlot.MAIN_HAND,
                item = equippedItems[EquipmentSlot.MAIN_HAND],
                onClick = onSlotClick,
                size = 72.dp
            )
            EquipmentSlotBox(
                slot = EquipmentSlot.HANDS,
                item = equippedItems[EquipmentSlot.HANDS],
                onClick = onSlotClick,
                size = 60.dp
            )
        }

        // --- Right Column ---
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            EquipmentSlotBox(
                slot = EquipmentSlot.AMULET,
                item = equippedItems[EquipmentSlot.AMULET],
                onClick = onSlotClick,
                size = 56.dp
            )
            EquipmentSlotBox(
                slot = EquipmentSlot.OFF_HAND,
                item = equippedItems[EquipmentSlot.OFF_HAND],
                onClick = onSlotClick,
                size = 72.dp
            )
            EquipmentSlotBox(
                slot = EquipmentSlot.RING,
                item = equippedItems[EquipmentSlot.RING],
                onClick = onSlotClick,
                size = 56.dp
            )
        }
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
            containerColor = if (item != null) {
                rarityColor.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            }
        ),
        border = BorderStroke(
            width = if (item != null) 2.dp else 1.5.dp,
            color = rarityColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (item != null) {
                val imageUrl = item.displayImageUrl
                val isGenerating = imageUrl?.startsWith("generating:") == true

                if (!imageUrl.isNullOrBlank()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = item.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(MaterialTheme.shapes.small),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            )
                        }
                    }
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
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val imageUrl = item.displayImageUrl
            val isGenerating = imageUrl?.startsWith("generating:") == true

            if (!imageUrl.isNullOrBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = item.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(MaterialTheme.shapes.small),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        )
                    }
                }
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
    onDelete: () -> Unit = {},
    onUpdate: (Item) -> Unit = {},
    onEvent: (CharacterDetailEvent) -> Unit = {}
) {
    var editedItem by remember { mutableStateOf(item) }
    val rarityColor = editedItem.rarity.toColor()

    // Sync editedItem when item from state changes (e.g. after generation completes)
    LaunchedEffect(item) {
        editedItem = item
    }

    val isGenerating = editedItem.imageUrl?.startsWith("generating:") == true

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 750.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header with big icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Card(
                            modifier = Modifier.size(78.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = rarityColor.copy(alpha = 0.1f)),
                            border = BorderStroke(2.dp, rarityColor)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (isGenerating) {
                                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                                } else if (!editedItem.displayImageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = editedItem.displayImageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    )
                                } else {
                                    Icon(
                                        imageVector = itemToIcon(editedItem),
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = rarityColor,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(12.dp))
                        Column {
                            if (isMasterMode) {
                                OutlinedTextField(
                                    value = editedItem.name,
                                    onValueChange = {
                                        editedItem = editedItem.copy(name = it)
                                        onUpdate(editedItem)
                                    },
                                    label = { Text("Name") },
                                    modifier = Modifier.width(180.dp)
                                )
                            } else {
                                Text(
                                    text = editedItem.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = editedItem.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = rarityColor,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (isMasterMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // AI Generation Section
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(DndIcons.Filled.AutoFixHigh, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(12.dp))
                                    Text("AI Image Generation", style = MaterialTheme.typography.labelLarge)
                                }
                                IconButton(
                                    onClick = { onEvent(CharacterDetailEvent.GenerateItemImage(editedItem.id)) },
                                    enabled = !isGenerating,
                                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                                ) {
                                    if (isGenerating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            DndIcons.Filled.AutoFixHigh,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        OutlinedTextField(value = editedItem.type, onValueChange = {
                            editedItem = editedItem.copy(type = it)
                            onUpdate(editedItem)
                        }, label = { Text("Type") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedItem.cost, onValueChange = {
                            editedItem = editedItem.copy(cost = it)
                            onUpdate(editedItem)
                        }, label = { Text("Cost") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedItem.weight.toString(), onValueChange = {
                            val w = it.toDoubleOrNull() ?: 0.0
                            editedItem = editedItem.copy(weight = w)
                            onUpdate(editedItem)
                        }, label = { Text("Weight") }, modifier = Modifier.fillMaxWidth())

                        Text(
                            "Current Slot: ${editedItem.slot?.name ?: "None"}",
                            style = MaterialTheme.typography.labelSmall
                        )

                        OutlinedTextField(value = editedItem.description, onValueChange = {
                            editedItem = editedItem.copy(description = it)
                            onUpdate(editedItem)
                        }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                        OutlinedTextField(value = editedItem.imageUrl ?: "", onValueChange = {
                            editedItem = editedItem.copy(
                                imageUrl = it.ifBlank {
                                    null
                                }
                            )
                            onUpdate(editedItem)
                        }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
                    }
                } else {
                    if (editedItem.slot != null) {
                        DetailRow("Slot", editedItem.slot!!.name.replace("_", " "))
                    }

                    if (editedItem.description.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = editedItem.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val canEquip = editedItem.slot != null
                    Button(
                        onClick = onToggleEquip,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canEquip || editedItem.equipped
                    ) {
                        Text(if (editedItem.equipped) "Unequip" else "Equip")
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
