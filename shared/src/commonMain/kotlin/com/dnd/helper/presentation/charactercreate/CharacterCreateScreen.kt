package com.dnd.helper.presentation.charactercreate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.random.Random
import com.dnd.helper.data.remote.dto.common.ApiReferenceDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreateScreen(
    onBackClick: () -> Unit,
    onCharacterCreated: () -> Unit,
) {
    val viewModelKey = remember { Random.nextLong().toString() }
    val viewModel: CharacterCreateViewModel = koinViewModel(key = viewModelKey)
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isSaved) {
        onCharacterCreated()
        return
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // COLUMN 1: Basic Info
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DesktopCardSection(title = "Basic Info", icon = Icons.Default.Person) {
                        // Image Generator Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.imageUrl.isNotBlank() && !state.imageUrl.startsWith("generating")) {
                                AsyncImage(
                                    model = state.imageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            
                            if (state.imageUrl.startsWith("generating")) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(
                                        onClick = { viewModel.onEvent(CharacterCreateEvent.GenerateImage) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Generate AI Image")
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.aiPrompt,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.AiPromptChanged(it)) },
                            label = { Text("AI Prompt (Auto-generated)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.aiWidth.toString(),
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.AiSizeChanged(it.toIntOrNull() ?: 1024, state.aiHeight)) },
                                label = { Text("Width") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state.aiHeight.toString(),
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.AiSizeChanged(state.aiWidth, it.toIntOrNull() ?: 1024)) },
                                label = { Text("Height") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = state.name,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.NameChanged(it)) },
                                label = { Text("Character Name") },
                                modifier = Modifier.weight(2f),
                                singleLine = true
                            )
                            Row(
                                modifier = Modifier.weight(1f).height(56.dp).border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraSmall),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.LevelChanged((state.level.toIntOrNull() ?: 1).minus(1).coerceAtLeast(1).toString())) }) {
                                    Icon(Icons.Default.Remove, null)
                                }
                                Text("Lvl ${state.level}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.LevelChanged((state.level.toIntOrNull() ?: 1).plus(1).coerceAtMost(20).toString())) }) {
                                    Icon(Icons.Default.Add, null)
                                }
                            }
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DropdownMenuField(
                                label = "Race",
                                value = state.race,
                                options = state.availableRaces,
                                optionLabel = { it.name },
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.RaceChanged(it)) },
                                modifier = Modifier.weight(1f)
                            )
                            DropdownMenuField(
                                label = "Subrace",
                                value = state.subrace,
                                options = state.availableSubraces,
                                optionLabel = { it.name },
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.SubraceChanged(it)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DropdownMenuField(
                                label = "Class",
                                value = state.characterClass,
                                options = state.availableClasses,
                                optionLabel = { it.name },
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.ClassChanged(it)) },
                                modifier = Modifier.weight(1f)
                            )
                            DropdownMenuField(
                                label = "Subclass",
                                value = state.subclass,
                                options = state.availableSubclasses,
                                optionLabel = { it.name },
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.SubclassChanged(it)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        DropdownMenuField(
                            label = "Background",
                            value = state.background,
                            options = state.availableBackgrounds,
                            optionLabel = { it.name },
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.BackgroundChanged(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenuField(
                            label = "Alignment",
                            value = state.alignment,
                            options = state.availableAlignments,
                            optionLabel = { it.name },
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.AlignmentChanged(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        MultiSelectDropdownField(
                            label = "Language",
                            selectedItems = state.selectedLanguages,
                            options = state.availableLanguages,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(CharacterCreateEvent.AddLanguage(it)) },
                            onRemove = { viewModel.onEvent(CharacterCreateEvent.RemoveLanguage(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.DescriptionChanged(it)) },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }
            }

            // COLUMN 2: Ability Scores
            LazyColumn(
                modifier = Modifier.weight(1.3f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DesktopCardSection(title = "Ability Scores") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                AbilityScoreBox("STR", state.strength, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.StrengthChanged(it)) }
                                AbilityScoreBox("DEX", state.dexterity, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.DexterityChanged(it)) }
                                AbilityScoreBox("CON", state.constitution, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.ConstitutionChanged(it)) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                AbilityScoreBox("INT", state.intelligence, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.IntelligenceChanged(it)) }
                                AbilityScoreBox("WIS", state.wisdom, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.WisdomChanged(it)) }
                                AbilityScoreBox("CHA", state.charisma, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.CharismaChanged(it)) }
                            }
                        }
                    }
                }
                item {
                    DesktopCardSection(title = "Combat & HP") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.maxHp,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.MaxHpChanged(it)) },
                                label = { Text("Max HP") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.armorClass,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.ArmorClassChanged(it)) },
                                label = { Text("Armor Class") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.initiative,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.InitiativeChanged(it)) },
                                label = { Text("Initiative") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.speed,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.SpeedChanged(it)) },
                                label = { Text("Speed") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }

            // COLUMN 3: Proficiencies & Features
            LazyColumn(
                modifier = Modifier.weight(1.3f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DesktopCardSection(title = "Proficiencies") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.savingThrows,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.SavingThrowsChanged(it)) },
                                label = { Text("Saving Throws") },
                                modifier = Modifier.weight(1f),
                            )
                            OutlinedTextField(
                                value = state.hitDice,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.HitDiceChanged(it)) },
                                label = { Text("Hit Die") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Spacer(Modifier.height(8.dp))
                        MultiSelectDropdownField(
                            label = "Skill",
                            selectedItems = state.selectedSkills,
                            options = state.availableSkills,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(CharacterCreateEvent.AddProficiencySkill(it)) },
                            onRemove = { viewModel.onEvent(CharacterCreateEvent.RemoveProficiencySkill(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = state.armorProficiencies,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ArmorProficienciesChanged(it)) },
                            label = { Text("Armor") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        MultiSelectDropdownField(
                            label = "Weapon Proficiency",
                            selectedItems = state.selectedWeapons,
                            options = state.availableEquipment,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(CharacterCreateEvent.AddProficiencyWeapon(it)) },
                            onRemove = { viewModel.onEvent(CharacterCreateEvent.RemoveProficiencyWeapon(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))

                        MultiSelectDropdownField(
                            label = "Tool Proficiency",
                            selectedItems = state.selectedTools,
                            options = state.availableEquipment,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(CharacterCreateEvent.AddProficiencyTool(it)) },
                            onRemove = { viewModel.onEvent(CharacterCreateEvent.RemoveProficiencyTool(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    DesktopCardSection(title = "Features & Traits") {
                        MultiSelectDropdownField(
                            label = "Class Feature",
                            selectedItems = state.selectedClassFeatures,
                            options = state.availableFeatures,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(CharacterCreateEvent.AddClassFeature(it)) },
                            onRemove = { viewModel.onEvent(CharacterCreateEvent.RemoveClassFeature(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        MultiSelectDropdownField(
                            label = "Racial Trait",
                            selectedItems = state.selectedRacialTraits,
                            options = state.availableTraits,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(CharacterCreateEvent.AddRacialTrait(it)) },
                            onRemove = { viewModel.onEvent(CharacterCreateEvent.RemoveRacialTrait(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        MultiSelectDropdownField(
                            label = "Feat",
                            selectedItems = state.selectedFeats,
                            options = state.availableFeats,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(CharacterCreateEvent.AddFeat(it)) },
                            onRemove = { viewModel.onEvent(CharacterCreateEvent.RemoveFeat(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Save Button at bottom right
        Column(
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            val errorMsg = state.error
            if (!errorMsg.isNullOrBlank()) {
                Text(
                    text = errorMsg,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Button(
                onClick = { viewModel.onEvent(CharacterCreateEvent.SaveCharacter) },
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text("Save Character", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMenuField(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )
        if (options.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    val text = optionLabel(option)
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = {
                            onValueChange(text)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DesktopCardSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun AbilityScoreBox(label: String, value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.width(60.dp).padding(top = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <T> MultiSelectDropdownField(
    label: String,
    selectedItems: List<String>,
    options: List<T>,
    optionLabel: (T) -> String,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    
    val filteredOptions = options.map(optionLabel).filter { 
        it.contains(searchText, ignoreCase = true) && !selectedItems.contains(it) 
    }

    Column(modifier = modifier) {
        if (selectedItems.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                selectedItems.forEach { item ->
                    InputChip(
                        selected = true,
                        onClick = { onRemove(item) },
                        label = { Text(item) },
                        trailingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                    )
                }
            }
        }
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchText,
                onValueChange = { 
                    searchText = it
                    expanded = true
                },
                label = { Text("Add $label") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                singleLine = true
            )
            
            if (filteredOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    filteredOptions.take(10).forEach { optionText ->
                        DropdownMenuItem(
                            text = { Text(optionText) },
                            onClick = {
                                onAdd(optionText)
                                searchText = ""
                                expanded = false
                            }
                        )
                    }
                    if (searchText.isNotBlank() && !filteredOptions.contains(searchText)) {
                        DropdownMenuItem(
                            text = { Text("Add custom: \"$searchText\"") },
                            onClick = {
                                onAdd(searchText)
                                searchText = ""
                                expanded = false
                            }
                        )
                    }
                }
            } else if (searchText.isNotBlank()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add custom: \"$searchText\"") },
                        onClick = {
                            onAdd(searchText)
                            searchText = ""
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
