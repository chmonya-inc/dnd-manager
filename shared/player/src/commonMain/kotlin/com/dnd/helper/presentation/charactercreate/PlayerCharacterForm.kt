package com.dnd.helper.presentation.charactercreate

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.theme.DndIcons

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PlayerCharacterForm(
    state: PlayerCharacterCreateState,
    onEvent: (PlayerCharacterCreateEvent) -> Unit,
    onBackClick: () -> Unit,
    onSaved: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    // Navigate on success
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            val id = state.savedCharacterId ?: state.name
            onSaved(id)
        }
    }

    // Show error in snackbar
    LaunchedEffect(state.error) {
        val err = state.error
        if (err != null) {
            snackbarHostState.showSnackbar(err)
            onEvent(PlayerCharacterCreateEvent.DismissError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) "Edit Character" else "New Character",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(4.dp))

                // ── Basic Info ──
                SectionHeader("Basic Info")

                StringField(
                    label = "Character Name *",
                    value = state.name,
                    onValueChange = { onEvent(PlayerCharacterCreateEvent.NameChanged(it)) }
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DropdownField(
                        label = "Race *",
                        value = state.race,
                        options = state.availableRaces,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.RaceChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownField(
                        label = "Subrace",
                        value = state.subrace,
                        options = state.availableSubraces,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.SubraceChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DropdownField(
                        label = "Class *",
                        value = state.characterClass,
                        options = state.availableClasses,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.ClassChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownField(
                        label = "Subclass",
                        value = state.subclass,
                        options = state.availableSubclasses,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.SubclassChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DropdownField(
                        label = "Background",
                        value = state.background,
                        options = state.availableBackgrounds,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.BackgroundChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                    DropdownField(
                        label = "Alignment",
                        value = state.alignment,
                        options = state.availableAlignments,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.AlignmentChanged(it)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        label = "Level",
                        value = state.level,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.LevelChanged(it)) },
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                    StringField(
                        label = "Max HP",
                        value = state.maxHp,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.MaxHpChanged(it)) },
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                    StringField(
                        label = "Current HP",
                        value = state.currentHp,
                        onValueChange = { onEvent(PlayerCharacterCreateEvent.CurrentHpChanged(it)) },
                        modifier = Modifier.weight(1f),
                        keyboardType = KeyboardType.Number
                    )
                }
                StringField(
                    label = "Description",
                    value = state.description,
                    onValueChange = { onEvent(PlayerCharacterCreateEvent.DescriptionChanged(it)) },
                    minLines = 2
                )

                Spacer(Modifier.height(4.dp))

                // ── Ability Scores ──
                SectionHeader("Ability Scores")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        "STR",
                        state.strength,
                        { onEvent(PlayerCharacterCreateEvent.StrengthChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "DEX",
                        state.dexterity,
                        { onEvent(PlayerCharacterCreateEvent.DexterityChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "CON",
                        state.constitution,
                        { onEvent(PlayerCharacterCreateEvent.ConstitutionChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        "INT",
                        state.intelligence,
                        { onEvent(PlayerCharacterCreateEvent.IntelligenceChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "WIS",
                        state.wisdom,
                        { onEvent(PlayerCharacterCreateEvent.WisdomChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "CHA",
                        state.charisma,
                        { onEvent(PlayerCharacterCreateEvent.CharismaChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ── Combat ──
                SectionHeader("Combat")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        "AC",
                        state.armorClass,
                        { onEvent(PlayerCharacterCreateEvent.ArmorClassChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "Initiative",
                        state.initiative,
                        { onEvent(PlayerCharacterCreateEvent.InitiativeChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "Speed",
                        state.speed,
                        { onEvent(PlayerCharacterCreateEvent.SpeedChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        "Temp HP",
                        state.tempHp,
                        { onEvent(PlayerCharacterCreateEvent.TempHpChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "Hit Dice",
                        state.hitDice,
                        { onEvent(PlayerCharacterCreateEvent.HitDiceChanged(it)) },
                        Modifier.weight(1f)
                    )
                    StringField(
                        "HD Current",
                        state.hitDiceCurrent,
                        { onEvent(PlayerCharacterCreateEvent.HitDiceCurrentChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        "Prof. Bonus",
                        state.proficiencyBonus,
                        { onEvent(PlayerCharacterCreateEvent.ProficiencyBonusChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "Exhaustion",
                        state.exhaustion,
                        { onEvent(PlayerCharacterCreateEvent.ExhaustionChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "Conditions",
                        state.conditions,
                        { onEvent(PlayerCharacterCreateEvent.ConditionsChanged(it)) },
                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ── Appearance ──
                SectionHeader("Appearance")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        "Age",
                        state.age,
                        { onEvent(PlayerCharacterCreateEvent.AgeChanged(it)) },
                        Modifier.weight(1f),
                        KeyboardType.Number
                    )
                    StringField(
                        "Gender",
                        state.gender,
                        { onEvent(PlayerCharacterCreateEvent.GenderChanged(it)) },
                        Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        "Height",
                        state.height,
                        { onEvent(PlayerCharacterCreateEvent.HeightChanged(it)) },
                        Modifier.weight(1f)
                    )
                    StringField(
                        "Weight",
                        state.weight,
                        { onEvent(PlayerCharacterCreateEvent.WeightChanged(it)) },
                        Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StringField(
                        "Eyes",
                        state.eyes,
                        { onEvent(PlayerCharacterCreateEvent.EyesChanged(it)) },
                        Modifier.weight(1f)
                    )
                    StringField(
                        "Hair",
                        state.hair,
                        { onEvent(PlayerCharacterCreateEvent.HairChanged(it)) },
                        Modifier.weight(1f)
                    )
                    StringField(
                        "Skin",
                        state.skin,
                        { onEvent(PlayerCharacterCreateEvent.SkinChanged(it)) },
                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ── Proficiencies ──
                SectionHeader("Proficiencies")
                StringField(
                    label = "Saving Throws (comma-separated)",
                    value = state.savingThrows,
                    onValueChange = { onEvent(PlayerCharacterCreateEvent.SavingThrowsChanged(it)) }
                )
                StringField(
                    label = "Armor Proficiencies (comma-separated)",
                    value = state.armorProficiencies,
                    onValueChange = { onEvent(PlayerCharacterCreateEvent.ArmorProficienciesChanged(it)) }
                )

                MultiSelectChips(
                    label = "Skills",
                    selected = state.selectedSkills,
                    options = state.availableSkills,
                    onAdd = { onEvent(PlayerCharacterCreateEvent.AddProficiencySkill(it)) },
                    onRemove = { onEvent(PlayerCharacterCreateEvent.RemoveProficiencySkill(it)) }
                )
                MultiSelectChips(
                    label = "Languages",
                    selected = state.selectedLanguages,
                    options = state.availableLanguages,
                    onAdd = { onEvent(PlayerCharacterCreateEvent.AddLanguage(it)) },
                    onRemove = { onEvent(PlayerCharacterCreateEvent.RemoveLanguage(it)) }
                )
                MultiSelectChips(
                    label = "Weapons",
                    selected = state.selectedWeapons,
                    options = state.availableEquipment,
                    onAdd = { onEvent(PlayerCharacterCreateEvent.AddProficiencyWeapon(it)) },
                    onRemove = { onEvent(PlayerCharacterCreateEvent.RemoveProficiencyWeapon(it)) }
                )
                MultiSelectChips(
                    label = "Tools",
                    selected = state.selectedTools,
                    options = state.availableEquipment,
                    onAdd = { onEvent(PlayerCharacterCreateEvent.AddProficiencyTool(it)) },
                    onRemove = { onEvent(PlayerCharacterCreateEvent.RemoveProficiencyTool(it)) }
                )

                Spacer(Modifier.height(4.dp))

                // ── Features ──
                SectionHeader("Features & Traits")
                MultiSelectChips(
                    label = "Class Features",
                    selected = state.selectedClassFeatures,
                    options = state.availableFeatures,
                    onAdd = { onEvent(PlayerCharacterCreateEvent.AddClassFeature(it)) },
                    onRemove = { onEvent(PlayerCharacterCreateEvent.RemoveClassFeature(it)) }
                )
                MultiSelectChips(
                    label = "Racial Traits",
                    selected = state.selectedRacialTraits,
                    options = state.availableTraits,
                    onAdd = { onEvent(PlayerCharacterCreateEvent.AddRacialTrait(it)) },
                    onRemove = { onEvent(PlayerCharacterCreateEvent.RemoveRacialTrait(it)) }
                )
                MultiSelectChips(
                    label = "Feats",
                    selected = state.selectedFeats,
                    options = state.availableFeats,
                    onAdd = { onEvent(PlayerCharacterCreateEvent.AddFeat(it)) },
                    onRemove = { onEvent(PlayerCharacterCreateEvent.RemoveFeat(it)) }
                )

                Spacer(Modifier.height(4.dp))

                // ── Items ──
                SectionHeader("Items")
                state.items.forEachIndexed { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StringField(
                                    label = "Name",
                                    value = item.name,
                                    onValueChange = { onEvent(PlayerCharacterCreateEvent.ItemNameChanged(index, it)) },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onEvent(PlayerCharacterCreateEvent.RemoveItem(index)) }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            }
                            StringField(
                                label = "Description",
                                value = item.description,
                                onValueChange = {
                                    onEvent(
                                        PlayerCharacterCreateEvent.ItemDescriptionChanged(index, it)
                                    )
                                },
                                minLines = 2
                            )
                        }
                    }
                }
                Button(
                    onClick = { onEvent(PlayerCharacterCreateEvent.AddItem) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item")
                }

                Spacer(Modifier.height(4.dp))

                // ── Weapons ──
                SectionHeader("Weapons")
                state.weapons.forEachIndexed { index, weapon ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StringField(
                                    label = "Name",
                                    value = weapon.name,
                                    onValueChange = {
                                        onEvent(
                                            PlayerCharacterCreateEvent.WeaponNameChanged(index, it)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onEvent(PlayerCharacterCreateEvent.RemoveWeapon(index)) }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StringField(
                                    "Attack Bonus",
                                    weapon.attackBonus,
                                    { onEvent(PlayerCharacterCreateEvent.WeaponAttackBonusChanged(index, it)) },
                                    Modifier.weight(1f)
                                )
                                StringField(
                                    "Damage",
                                    weapon.damage,
                                    { onEvent(PlayerCharacterCreateEvent.WeaponDamageChanged(index, it)) },
                                    Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StringField(
                                    "Damage Type",
                                    weapon.damageType,
                                    { onEvent(PlayerCharacterCreateEvent.WeaponDamageTypeChanged(index, it)) },
                                    Modifier.weight(1f)
                                )
                            }
                            StringField(
                                "Notes",
                                weapon.notes,
                                { onEvent(PlayerCharacterCreateEvent.WeaponNotesChanged(index, it)) },
                                minLines = 2
                            )
                        }
                    }
                }
                Button(
                    onClick = { onEvent(PlayerCharacterCreateEvent.AddWeapon) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Weapon")
                }

                Spacer(Modifier.height(4.dp))

                // ── Spells ──
                SectionHeader("Spells")
                state.spellList.forEachIndexed { index, spell ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StringField(
                                    label = "Spell Name",
                                    value = spell.name,
                                    onValueChange = { onEvent(PlayerCharacterCreateEvent.SpellNameChanged(index, it)) },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onEvent(PlayerCharacterCreateEvent.RemoveSpell(index)) }) {
                                    Icon(Icons.Default.Delete, "Delete")
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StringField(
                                    "Level",
                                    spell.level.toString(),
                                    { onEvent(PlayerCharacterCreateEvent.SpellLevelChanged(index, it)) },
                                    Modifier.weight(1f),
                                    KeyboardType.Number
                                )
                                StringField(
                                    "School",
                                    spell.school,
                                    { onEvent(PlayerCharacterCreateEvent.SpellSchoolChanged(index, it)) },
                                    Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StringField(
                                    "Casting Time",
                                    spell.castingTime,
                                    { onEvent(PlayerCharacterCreateEvent.SpellCastingTimeChanged(index, it)) },
                                    Modifier.weight(1f)
                                )
                                StringField(
                                    "Range",
                                    spell.range,
                                    { onEvent(PlayerCharacterCreateEvent.SpellRangeChanged(index, it)) },
                                    Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StringField(
                                    "Duration",
                                    spell.duration,
                                    { onEvent(PlayerCharacterCreateEvent.SpellDurationChanged(index, it)) },
                                    Modifier.weight(1f)
                                )
                                StringField(
                                    "Damage",
                                    spell.damage,
                                    { onEvent(PlayerCharacterCreateEvent.SpellDamageChanged(index, it)) },
                                    Modifier.weight(1f)
                                )
                            }
                            StringField(
                                "Description",
                                spell.description,
                                { onEvent(PlayerCharacterCreateEvent.SpellDescriptionChanged(index, it)) },
                                minLines = 2
                            )
                        }
                    }
                }
                Button(
                    onClick = { onEvent(PlayerCharacterCreateEvent.AddSpell) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add Spell")
                }

                Spacer(Modifier.height(8.dp))

                // ── Save Button ──
                Button(
                    onClick = { onEvent(PlayerCharacterCreateEvent.SaveCharacter) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (state.isSaving) "Saving…" else if (state.isEditMode) "Update Character" else "Create Character"
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Helper Composables ──

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun StringField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = minLines == 1,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
fun DropdownField(
    label: String,
    value: String,
    options: List<ApiReferenceDto>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    val allNames = remember(options) { options.map { it.name } }

    val filtered = remember(expanded, options) {
        if (value.isBlank()) {
            allNames
        } else {
            allNames.filter { it.contains(value, ignoreCase = true) }
        }
    }

    Column(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) DndIcons.Filled.ExpandLess else DndIcons.Filled.ExpandMore,
                        contentDescription = "Show options"
                    )
                }
            }
        )
        DropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filtered.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onValueChange(name)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectChips(
    label: String,
    selected: List<String>,
    options: List<ApiReferenceDto>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val allNames = remember(options) { options.map { it.name } }

    val filtered = remember(searchText, allNames, selected) {
        allNames.filter {
            it.contains(searchText, ignoreCase = true) && !selected.contains(it)
        }
    }

    val canAddCustom = searchText.isNotBlank() && !selected.contains(searchText.trim())

    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        if (selected.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                selected.forEach { item ->
                    InputChip(
                        selected = true,
                        onClick = { onRemove(item) },
                        label = { Text(item) },
                        trailingIcon = {
                            Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Add $label") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardActions = KeyboardActions(
                onDone = {
                    if (canAddCustom) {
                        onAdd(searchText.trim())
                        searchText = ""
                    }
                }
            ),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (canAddCustom) {
                        IconButton(onClick = {
                            onAdd(searchText.trim())
                            searchText = ""
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add custom value")
                        }
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) DndIcons.Filled.ExpandLess else DndIcons.Filled.ExpandMore,
                            contentDescription = "Show options"
                        )
                    }
                }
            }
        )
        DropdownMenu(
            expanded = expanded && filtered.isNotEmpty(),
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filtered.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onAdd(name)
                        searchText = ""
                        expanded = false
                    }
                )
            }
        }
    }
}
