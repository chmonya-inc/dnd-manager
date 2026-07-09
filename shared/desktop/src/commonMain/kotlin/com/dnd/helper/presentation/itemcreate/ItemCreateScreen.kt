package com.dnd.helper.presentation.itemcreate

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.ItemRarity
import com.dnd.helper.presentation.charactercreate.DropdownMenuField
import com.dnd.helper.presentation.charactercreate.MultiSelectDropdownField
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemCreateScreen(
    existingItem: com.dnd.helper.domain.model.Item? = null,
    ownerId: String? = null,
    viewModel: ItemCreateViewModel = koinInject(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(existingItem, ownerId) {
        viewModel.initData(existingItem, ownerId)
    }

    LaunchedEffect(state.isSaveSuccessful) {
        if (state.isSaveSuccessful) {
            onNavigateBack()
        }
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { viewModel.onEvent(ItemCreateEvent.SaveClicked) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Save Item")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.saveError != null) {
                Text(
                    text = state.saveError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            DropdownMenuField(
                label = "Owner",
                value = state.characters.find { it.id == state.characterId }?.name ?: "None",
                options = state.characters.map { it.name } + listOf("None"),
                optionLabel = { it },
                onValueChange = { name ->
                    val char = state.characters.find { it.name == name }
                    if (char != null) {
                        viewModel.onEvent(ItemCreateEvent.OwnerChanged(char.id))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = { viewModel.onEvent(ItemCreateEvent.NameChanged(it)) },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.description,
                onValueChange = { viewModel.onEvent(ItemCreateEvent.DescriptionChanged(it)) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = state.cost,
                    onValueChange = { viewModel.onEvent(ItemCreateEvent.CostChanged(it)) },
                    label = { Text("Cost (e.g., 50 gp)") },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = state.weight,
                    onValueChange = { viewModel.onEvent(ItemCreateEvent.WeightChanged(it)) },
                    label = { Text("Weight (lbs)") },
                    modifier = Modifier.weight(1f)
                )
            }

            DropdownMenuField(
                label = "Rarity",
                value = state.rarity.name,
                options = ItemRarity.entries.map { it.name },
                optionLabel = { it },
                onValueChange = { text ->
                    runCatching { ItemRarity.valueOf(text) }.getOrNull()
                        ?.let { viewModel.onEvent(ItemCreateEvent.RarityChanged(it)) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenuField(
                label = "Equipment Slot",
                value = state.slot?.name ?: "None",
                options = EquipmentSlot.entries.map { it.name } + listOf("None"),
                optionLabel = { it },
                onValueChange = { text ->
                    val slot = if (text == "None") null
                               else runCatching { EquipmentSlot.valueOf(text) }.getOrNull()
                    if (text == "None" || slot != null) {
                        viewModel.onEvent(ItemCreateEvent.SlotChanged(slot))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            DropdownMenuField(
                label = "Item Type",
                value = state.type.ifBlank { "Select Type" },
                options = state.availableEquipmentCategories,
                optionLabel = { it },
                onValueChange = { viewModel.onEvent(ItemCreateEvent.TypeChanged(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            MultiSelectDropdownField(
                label = "Properties",
                selectedItems = state.properties,
                options = state.availableProperties,
                optionLabel = { it },
                onAdd = { viewModel.onEvent(ItemCreateEvent.PropertyToggled(it)) },
                onRemove = { viewModel.onEvent(ItemCreateEvent.PropertyToggled(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(
                    checked = state.isEquipped,
                    onCheckedChange = { viewModel.onEvent(ItemCreateEvent.EquippedChanged(it)) }
                )
                Text("Currently Equipped")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text("AI Generation", style = MaterialTheme.typography.titleMedium)
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.imageUrl,
                    onValueChange = { viewModel.onEvent(ItemCreateEvent.ImageUrlChanged(it)) },
                    label = { Text("Image URL") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                )
                OutlinedTextField(value = state.aiWidth, onValueChange = { viewModel.onEvent(ItemCreateEvent.AiWidthChanged(it)) }, label = { Text("W") }, modifier = Modifier.width(70.dp), shape = MaterialTheme.shapes.small, singleLine = true)
                OutlinedTextField(value = state.aiHeight, onValueChange = { viewModel.onEvent(ItemCreateEvent.AiHeightChanged(it)) }, label = { Text("H") }, modifier = Modifier.width(70.dp), shape = MaterialTheme.shapes.small, singleLine = true)
                com.dnd.helper.presentation.desktop.ImageGenerationButton(
                    prompt = state.aiPrompt,
                    onImageUrlGenerated = { viewModel.onEvent(ItemCreateEvent.ImageUrlChanged(it)) },
                    accentColor = MaterialTheme.colorScheme.primary,
                    entityId = state.itemId ?: "",
                    entityType = "item",
                    width = state.aiWidth.toIntOrNull(),
                    height = state.aiHeight.toIntOrNull()
                )
            }
            
            OutlinedTextField(
                value = state.aiPrompt,
                onValueChange = { viewModel.onEvent(ItemCreateEvent.AiPromptChanged(it)) },
                label = { Text("AI Image Prompt") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
