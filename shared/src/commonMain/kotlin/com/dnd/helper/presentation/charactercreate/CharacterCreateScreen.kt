package com.dnd.helper.presentation.charactercreate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.ItemRarity
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreateScreen(
    onBackClick: () -> Unit,
    onCharacterCreated: () -> Unit,
) {
    val viewModel: CharacterCreateViewModel = koinViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isSaved) {
        onCharacterCreated()
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Character") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ===== Basic Info =====
            item { SectionTitle("Basic Information") }
            item {
                CardSection {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.NameChanged(it)) },
                            label = { Text("Character Name") },
                        )
                        OutlinedTextField(
                            value = state.playerName,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.PlayerNameChanged(it)) },
                            label = { Text("Player Name") },
                        )
                        OutlinedTextField(
                            value = state.race,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.RaceChanged(it)) },
                            label = { Text("Race") },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = state.characterClass,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ClassChanged(it)) },
                            label = { Text("Class") },
                            )
                        OutlinedTextField(
                            value = state.subclass,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SubclassChanged(it)) },
                            label = { Text("Subclass") },
                            )
                        OutlinedTextField(
                            value = state.background,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.BackgroundChanged(it)) },
                            label = { Text("Background") },
                        )
                        OutlinedTextField(
                            value = state.level,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.LevelChanged(it)) },
                            label = { Text("Level") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.experiencePoints,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ExperiencePointsChanged(it)) },
                        label = { Text("Experience Points") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.DescriptionChanged(it)) },
                        label = { Text("Description / Bio") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.imageUrl,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ImageUrlChanged(it)) },
                        label = { Text("Image URL (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ===== Appearance =====
            item { SectionTitle("Appearance") }
            item {
                CardSection {
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.age,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.AgeChanged(it)) },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.gender,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.GenderChanged(it)) },
                            label = { Text("Gender") },
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.height,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.HeightChanged(it)) },
                            label = { Text("Height") },
                            )
                        OutlinedTextField(
                            value = state.weight,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.WeightChanged(it)) },
                            label = { Text("Weight") },
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.eyes,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.EyesChanged(it)) },
                            label = { Text("Eyes") },
                            )
                        OutlinedTextField(
                            value = state.hair,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.HairChanged(it)) },
                            label = { Text("Hair") },
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.skin,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkinChanged(it)) },
                        label = { Text("Skin") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ===== Ability Scores =====
            item { SectionTitle("Ability Scores") }
            item {
                CardSection {
                    TwoColumnRow {
                        AbilityScoreField(
                            label = "Strength",
                            value = state.strength,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.StrengthChanged(it)) },
                        )
                        AbilityScoreField(
                            label = "Dexterity",
                            value = state.dexterity,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.DexterityChanged(it)) },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        AbilityScoreField(
                            label = "Constitution",
                            value = state.constitution,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ConstitutionChanged(it)) },
                        )
                        AbilityScoreField(
                            label = "Intelligence",
                            value = state.intelligence,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.IntelligenceChanged(it)) },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        AbilityScoreField(
                            label = "Wisdom",
                            value = state.wisdom,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.WisdomChanged(it)) },
                        )
                        AbilityScoreField(
                            label = "Charisma",
                            value = state.charisma,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.CharismaChanged(it)) },
                        )
                    }
                }
            }

            // ===== Hit Points & Combat =====
            item { SectionTitle("Hit Points & Combat") }
            item {
                CardSection {
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.maxHp,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.MaxHpChanged(it)) },
                            label = { Text("Max HP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.currentHp,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.CurrentHpChanged(it)) },
                            label = { Text("Current HP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.tempHp,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.TempHpChanged(it)) },
                            label = { Text("Temp HP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.armorClass,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ArmorClassChanged(it)) },
                            label = { Text("Armor Class") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.initiative,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.InitiativeChanged(it)) },
                            label = { Text("Initiative") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.speed,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SpeedChanged(it)) },
                            label = { Text("Speed (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.proficiencyBonus,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ProficiencyBonusChanged(it)) },
                            label = { Text("Proficiency Bonus") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.hitDice,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.HitDiceChanged(it)) },
                            label = { Text("Hit Dice (e.g. 1d8)") },
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.hitDiceCurrent,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.HitDiceCurrentChanged(it)) },
                        label = { Text("Hit Dice Remaining") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ===== Status =====
            item { SectionTitle("Status & Conditions") }
            item {
                CardSection {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Inspiration", modifier = Modifier.fillMaxWidth(1f))
                        Switch(
                            checked = state.inspiration,
                            onCheckedChange = { viewModel.onEvent(CharacterCreateEvent.InspirationChanged(it)) },
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.exhaustion,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ExhaustionChanged(it)) },
                            label = { Text("Exhaustion (0-6)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.deathSaveSuccesses,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.DeathSaveSuccessesChanged(it)) },
                            label = { Text("Death Save Successes (0-3)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.deathSaveFailures,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.DeathSaveFailuresChanged(it)) },
                        label = { Text("Death Save Failures (0-3)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.conditions,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ConditionsChanged(it)) },
                        label = { Text("Conditions (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ===== Proficiencies =====
            item { SectionTitle("Proficiencies") }
            item {
                CardSection {
                    OutlinedTextField(
                        value = state.savingThrows,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.SavingThrowsChanged(it)) },
                        label = { Text("Saving Throws (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.skills,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillsChanged(it)) },
                        label = { Text("Skills (comma-separated)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.armorProficiencies,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ArmorProficienciesChanged(it)) },
                            label = { Text("Armor") },
                            )
                        OutlinedTextField(
                            value = state.weaponProficiencies,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.WeaponProficienciesChanged(it)) },
                            label = { Text("Weapons") },
                            )
                    }
                    Spacer(Modifier.height(8.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.toolProficiencies,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ToolProficienciesChanged(it)) },
                            label = { Text("Tools") },
                            )
                        OutlinedTextField(
                            value = state.languages,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.LanguagesChanged(it)) },
                            label = { Text("Languages") },
                            )
                    }
                }
            }

            // ===== Items =====
            item { SectionTitle("Items") }
            itemsIndexed(state.items) { index, item ->
                CardSection {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Item #${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            )
                        IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.RemoveItem(index)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove item")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = item.name,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ItemNameChanged(index, it)) },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    EquipmentSlotDropdown(
                        selected = item.slot,
                        onSelect = { viewModel.onEvent(CharacterCreateEvent.ItemSlotChanged(index, it)) },
                    )
                    Spacer(Modifier.height(4.dp))
                    ItemRarityDropdown(
                        selected = item.rarity,
                        onSelect = { viewModel.onEvent(CharacterCreateEvent.ItemRarityChanged(index, it)) },
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = item.description,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ItemDescriptionChanged(index, it)) },
                        label = { Text("Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.equipped,
                            onCheckedChange = { viewModel.onEvent(CharacterCreateEvent.ItemEquippedChanged(index, it)) },
                        )
                        Text("Equipped")
                    }
                }
            }
            item {
                Button(
                    onClick = { viewModel.onEvent(CharacterCreateEvent.AddItem) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item")
                }
            }

            // ===== Weapons =====
            item { SectionTitle("Weapons") }
            itemsIndexed(state.weapons) { index, weapon ->
                CardSection {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Weapon #${index + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            )
                        IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.RemoveWeapon(index)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove weapon")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = weapon.name,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.WeaponNameChanged(index, it)) },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = weapon.attackBonus,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.WeaponAttackBonusChanged(index, it)) },
                            label = { Text("Attack Bonus") },
                            )
                        OutlinedTextField(
                            value = weapon.damage,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.WeaponDamageChanged(index, it)) },
                            label = { Text("Damage") },
                            )
                    }
                    Spacer(Modifier.height(4.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = weapon.damageType,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.WeaponDamageTypeChanged(index, it)) },
                            label = { Text("Damage Type") },
                            )
                        OutlinedTextField(
                            value = weapon.notes,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.WeaponNotesChanged(index, it)) },
                            label = { Text("Notes") },
                            )
                    }
                }
            }
            item {
                Button(
                    onClick = { viewModel.onEvent(CharacterCreateEvent.AddWeapon) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Weapon")
                }
            }

            // ===== Features =====
            item { SectionTitle("Features & Traits") }
            item {
                CardSection {
                    OutlinedTextField(
                        value = state.classFeatures,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ClassFeaturesChanged(it)) },
                        label = { Text("Class Features (one per line)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.racialTraits,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.RacialTraitsChanged(it)) },
                        label = { Text("Racial Traits (one per line)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.feats,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.FeatsChanged(it)) },
                        label = { Text("Feats (one per line)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Error
            item {
                AnimatedVisibility(visible = state.error != null) {
                    state.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }

            // Save button
            item {
                Button(
                    onClick = { viewModel.onEvent(CharacterCreateEvent.SaveCharacter) },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Text("Saving...")
                    } else {
                        Text("Create Character")
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun CardSection(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
private fun TwoColumnRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun AbilityScoreField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipmentSlotDropdown(selected: EquipmentSlot?, onSelect: (EquipmentSlot?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(null) + EquipmentSlot.entries
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = selected?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "None",
            onValueChange = {},
            readOnly = true,
            label = { Text("Slot") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() }
                                ?: "None"
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemRarityDropdown(selected: ItemRarity, onSelect: (ItemRarity) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selected.name.lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Rarity") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            ItemRarity.entries.forEach { rarity ->
                DropdownMenuItem(
                    text = { Text(rarity.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onSelect(rarity)
                        expanded = false
                    },
                )
            }
        }
    }
}
