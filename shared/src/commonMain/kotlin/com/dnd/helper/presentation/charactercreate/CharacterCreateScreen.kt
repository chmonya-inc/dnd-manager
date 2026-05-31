package com.dnd.helper.presentation.charactercreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.Item
import com.dnd.helper.domain.model.ItemRarity

@Composable
fun CharacterCreateScreen(
    onBackClick: () -> Unit,
    onCharacterCreated: () -> Unit,
    viewModel: CharacterCreateViewModel = org.koin.compose.viewmodel.koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    if (state.isSaved) {
        onCharacterCreated()
    }

    CharacterCreateContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBackClick = onBackClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CharacterCreateContent(
    state: CharacterCreateState,
    onEvent: (CharacterCreateEvent) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Character") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { onEvent(CharacterCreateEvent.SaveCharacter) },
                        enabled = !state.isSaving,
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.width(20.dp).height(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Icon(Icons.Default.Check, null, modifier = Modifier.padding(end = 4.dp))
                            Text("Save")
                        }
                    }
                }
            )
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
            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Basic Info
            SectionTitle("Basic Info")
            OutlinedTextField(
                value = state.name,
                onValueChange = { onEvent(CharacterCreateEvent.NameChanged(it)) },
                label = { Text("Character Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.race,
                    onValueChange = { onEvent(CharacterCreateEvent.RaceChanged(it)) },
                    label = { Text("Race") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.characterClass,
                    onValueChange = { onEvent(CharacterCreateEvent.ClassChanged(it)) },
                    label = { Text("Class") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = state.level,
                    onValueChange = { onEvent(CharacterCreateEvent.LevelChanged(it)) },
                    label = { Text("Level") },
                    modifier = Modifier.width(80.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            OutlinedTextField(
                value = state.playerName,
                onValueChange = { onEvent(CharacterCreateEvent.PlayerNameChanged(it)) },
                label = { Text("Player Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.imageUrl,
                onValueChange = { onEvent(CharacterCreateEvent.ImageUrlChanged(it)) },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // HP
            SectionTitle("Hit Points")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.maxHp,
                    onValueChange = { onEvent(CharacterCreateEvent.MaxHpChanged(it)) },
                    label = { Text("Max HP") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                )
                OutlinedTextField(
                    value = state.currentHp,
                    onValueChange = { onEvent(CharacterCreateEvent.CurrentHpChanged(it)) },
                    label = { Text("Current HP") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                )
            }

            // Stats
            SectionTitle("Ability Scores")
            StatRow(
                label1 = "STR", value1 = state.strength, onValue1 = { onEvent(CharacterCreateEvent.StrengthChanged(it)) },
                label2 = "DEX", value2 = state.dexterity, onValue2 = { onEvent(CharacterCreateEvent.DexterityChanged(it)) },
                label3 = "CON", value3 = state.constitution, onValue3 = { onEvent(CharacterCreateEvent.ConstitutionChanged(it)) },
            )
            StatRow(
                label1 = "INT", value1 = state.intelligence, onValue1 = { onEvent(CharacterCreateEvent.IntelligenceChanged(it)) },
                label2 = "WIS", value2 = state.wisdom, onValue2 = { onEvent(CharacterCreateEvent.WisdomChanged(it)) },
                label3 = "CHA", value3 = state.charisma, onValue3 = { onEvent(CharacterCreateEvent.CharismaChanged(it)) },
            )

            // Description
            SectionTitle("Description")
            OutlinedTextField(
                value = state.description,
                onValueChange = { onEvent(CharacterCreateEvent.DescriptionChanged(it)) },
                label = { Text("Biography / Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                minLines = 3,
            )

            // Items
            SectionTitle("Items")
            Button(onClick = { onEvent(CharacterCreateEvent.AddItem) }) {
                Icon(Icons.Default.Add, null, modifier = Modifier.padding(end = 4.dp))
                Text("Add Item")
            }

            state.items.forEachIndexed { index, item ->
                ItemEditor(
                    index = index,
                    item = item,
                    onEvent = onEvent,
                )
            }

            if (state.items.isEmpty()) {
                Text(
                    text = "No items added yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onEvent(CharacterCreateEvent.SaveCharacter) },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(20.dp).height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Create Character")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun StatRow(
    label1: String, value1: String, onValue1: (String) -> Unit,
    label2: String, value2: String, onValue2: (String) -> Unit,
    label3: String, value3: String, onValue3: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = value1,
            onValueChange = onValue1,
            label = { Text(label1) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        )
        OutlinedTextField(
            value = value2,
            onValueChange = onValue2,
            label = { Text(label2) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        )
        OutlinedTextField(
            value = value3,
            onValueChange = onValue3,
            label = { Text(label3) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemEditor(
    index: Int,
    item: Item,
    onEvent: (CharacterCreateEvent) -> Unit,
) {
    var slotExpanded by remember { mutableStateOf(false) }
    var rarityExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = item.name,
                onValueChange = { onEvent(CharacterCreateEvent.ItemNameChanged(index, it)) },
                label = { Text("Item Name") },
                modifier = Modifier.weight(1f),
                singleLine = true,
            )
            IconButton(
                onClick = { onEvent(CharacterCreateEvent.RemoveItem(index)) }
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove item")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Slot dropdown
            ExposedDropdownMenuBox(
                expanded = slotExpanded,
                onExpandedChange = { slotExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = item.slot?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Slot") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = slotExpanded,
                    onDismissRequest = { slotExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            onEvent(CharacterCreateEvent.ItemSlotChanged(index, null))
                            slotExpanded = false
                        }
                    )
                    EquipmentSlot.entries.forEach { slot ->
                        DropdownMenuItem(
                            text = { Text(slot.name) },
                            onClick = {
                                onEvent(CharacterCreateEvent.ItemSlotChanged(index, slot))
                                slotExpanded = false
                            }
                        )
                    }
                }
            }

            // Rarity dropdown
            ExposedDropdownMenuBox(
                expanded = rarityExpanded,
                onExpandedChange = { rarityExpanded = it },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = item.rarity.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Rarity") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rarityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = rarityExpanded,
                    onDismissRequest = { rarityExpanded = false }
                ) {
                    ItemRarity.entries.forEach { rarity ->
                        DropdownMenuItem(
                            text = { Text(rarity.name) },
                            onClick = {
                                onEvent(CharacterCreateEvent.ItemRarityChanged(index, rarity))
                                rarityExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = item.equipped,
                onCheckedChange = { onEvent(CharacterCreateEvent.ItemEquippedChanged(index, it)) }
            )
            Text("Equipped", style = MaterialTheme.typography.bodyMedium)
        }

        OutlinedTextField(
            value = item.description,
            onValueChange = { onEvent(CharacterCreateEvent.ItemDescriptionChanged(index, it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}
