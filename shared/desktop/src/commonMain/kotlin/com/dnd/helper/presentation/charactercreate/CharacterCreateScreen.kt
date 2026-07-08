package com.dnd.helper.presentation.charactercreate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnd.helper.theme.DndIcons
import com.dnd.helper.theme.dndColors
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreateScreen(
    existingCharacter: com.dnd.helper.domain.model.Character? = null,
    onBackClick: () -> Unit,
    onCharacterCreated: () -> Unit,
) {
    val viewModelKey = remember { Random.nextLong().toString() }
    val viewModel: CharacterCreateViewModel = koinViewModel(key = viewModelKey)
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(existingCharacter) {
        existingCharacter?.let { viewModel.onEvent(CharacterCreateEvent.LoadCharacter(it)) }
    }

    if (state.isSaved) {
        onCharacterCreated()
        return
    }

    Scaffold(
        containerColor = Color(0xFF0A0F0A), // Dark background like monster creator
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF0A0F0A),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val errorMsg = state.error
                    if (!errorMsg.isNullOrBlank()) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    Button(
                        onClick = { viewModel.onEvent(CharacterCreateEvent.SaveCharacter) },
                        enabled = !state.isSaving,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (existingCharacter == null) "Create Character" else "Update Character", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
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
                                .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(8.dp))
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
                                CircularProgressIndicator(color = Color(0xFF4CAF50))
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Button(
                                        onClick = { viewModel.onEvent(CharacterCreateEvent.GenerateImage) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.8f), contentColor = Color.White)
                                    ) {
                                        Icon(DndIcons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Generate AI Image")
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        CharacterTextField(
                            value = state.imageUrl,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ImageUrlChanged(it)) },
                            label = "Image URL"
                        )
                        Spacer(Modifier.height(8.dp))
                        CharacterTextField(
                            value = state.aiPrompt,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.AiPromptChanged(it)) },
                            label = "AI Prompt (Auto-generated)",
                            minLines = 2
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            CharacterTextField(
                                value = state.aiWidth.toString(),
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.AiSizeChanged(it.toIntOrNull() ?: 1024, state.aiHeight)) },
                                label = "Width",
                                modifier = Modifier.weight(1f)
                            )
                            CharacterTextField(
                                value = state.aiHeight.toString(),
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.AiSizeChanged(state.aiWidth, it.toIntOrNull() ?: 1024)) },
                                label = "Height",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        
                        CharacterTextField(
                            value = state.sessionId,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SessionIdChanged(it)) },
                            label = "Session ID",
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            CharacterTextField(
                                value = state.name,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.NameChanged(it)) },
                                label = "Character Name",
                                modifier = Modifier.weight(2f)
                            )
                            Row(
                                modifier = Modifier.weight(1f).height(56.dp).border(1.dp, Color(0xFF2E7D32), MaterialTheme.shapes.extraSmall),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.LevelChanged((state.level.toIntOrNull() ?: 1).minus(1).coerceAtLeast(1).toString())) }) {
                                    Icon(Icons.Default.Remove, null, tint = Color.White)
                                }
                                Text("Lvl ${state.level}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White)
                                IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.LevelChanged((state.level.toIntOrNull() ?: 1).plus(1).coerceAtMost(20).toString())) }) {
                                    Icon(Icons.Default.Add, null, tint = Color.White)
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

                        CharacterTextField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.DescriptionChanged(it)) },
                            label = "Description",
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
                    DesktopCardSection(title = "Ability Scores", icon = Icons.Default.BarChart) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                AbilityScoreBox("STR", state.strength, dndColors.strength, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.StrengthChanged(it)) }
                                AbilityScoreBox("DEX", state.dexterity, dndColors.dexterity, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.DexterityChanged(it)) }
                                AbilityScoreBox("CON", state.constitution, dndColors.constitution, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.ConstitutionChanged(it)) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                AbilityScoreBox("INT", state.intelligence, dndColors.intelligence, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.IntelligenceChanged(it)) }
                                AbilityScoreBox("WIS", state.wisdom, dndColors.wisdom, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.WisdomChanged(it)) }
                                AbilityScoreBox("CHA", state.charisma, dndColors.charisma, Modifier.weight(1f)) { viewModel.onEvent(CharacterCreateEvent.CharismaChanged(it)) }
                            }
                        }
                    }
                }
                item {
                    DesktopCardSection(title = "Combat & HP", icon = Icons.Default.Shield) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CharacterTextField(
                                value = state.maxHp,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.MaxHpChanged(it)) },
                                label = "Max HP",
                                modifier = Modifier.weight(1f)
                            )
                            CharacterTextField(
                                value = state.armorClass,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.ArmorClassChanged(it)) },
                                label = "AC",
                                modifier = Modifier.weight(1f)
                            )
                            CharacterTextField(
                                value = state.initiative,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.InitiativeChanged(it)) },
                                label = "Init",
                                modifier = Modifier.weight(1f)
                            )
                            CharacterTextField(
                                value = state.speed,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.SpeedChanged(it)) },
                                label = "Spd",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                item {
                    DesktopCardSection(
                        title = "Spells", 
                        icon = Icons.Default.AutoFixHigh,
                        onAddClick = { viewModel.onEvent(CharacterCreateEvent.AddSpell) }
                    ) {
                        state.spellList.forEachIndexed { index, spell ->
                            SpellEditItem(
                                index = index,
                                spell = spell,
                                onEvent = viewModel::onEvent
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
                    DesktopCardSection(title = "Proficiencies", icon = Icons.AutoMirrored.Filled.List) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CharacterTextField(
                                value = state.savingThrows,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.SavingThrowsChanged(it)) },
                                label = "Saving Throws",
                                modifier = Modifier.weight(1f),
                            )
                            CharacterTextField(
                                value = state.hitDice,
                                onValueChange = { viewModel.onEvent(CharacterCreateEvent.HitDiceChanged(it)) },
                                label = "Hit Die",
                                modifier = Modifier.weight(1f),
                            )
                        }
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
                        
                        CharacterTextField(
                            value = state.armorProficiencies,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ArmorProficienciesChanged(it)) },
                            label = "Armor",
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
                    DesktopCardSection(title = "Features & Traits", icon = Icons.Default.Star) {
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
    }
}

@Composable
fun SpellEditItem(
    index: Int,
    spell: com.dnd.helper.domain.model.Spell,
    onEvent: (CharacterCreateEvent) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CharacterTextField(
                    value = spell.name,
                    onValueChange = { onEvent(CharacterCreateEvent.SpellNameChanged(index, it)) },
                    label = "Spell Name",
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onEvent(CharacterCreateEvent.RemoveSpell(index)) }) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.7f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CharacterTextField(
                    value = spell.level.toString(),
                    onValueChange = { onEvent(CharacterCreateEvent.SpellLevelChanged(index, it)) },
                    label = "Lvl",
                    modifier = Modifier.weight(1f)
                )
                CharacterTextField(
                    value = spell.school,
                    onValueChange = { onEvent(CharacterCreateEvent.SpellSchoolChanged(index, it)) },
                    label = "School",
                    modifier = Modifier.weight(2f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CharacterTextField(
                    value = spell.castingTime,
                    onValueChange = { onEvent(CharacterCreateEvent.SpellCastingTimeChanged(index, it)) },
                    label = "Casting Time",
                    modifier = Modifier.weight(1f)
                )
                CharacterTextField(
                    value = spell.range,
                    onValueChange = { onEvent(CharacterCreateEvent.SpellRangeChanged(index, it)) },
                    label = "Range",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CharacterTextField(
                    value = spell.duration,
                    onValueChange = { onEvent(CharacterCreateEvent.SpellDurationChanged(index, it)) },
                    label = "Duration",
                    modifier = Modifier.weight(1f)
                )
                CharacterTextField(
                    value = spell.damage,
                    onValueChange = { onEvent(CharacterCreateEvent.SpellDamageChanged(index, it)) },
                    label = "Damage (e.g. 1d8)",
                    modifier = Modifier.weight(1f)
                )
            }
            CharacterTextField(
                value = spell.description,
                onValueChange = { onEvent(CharacterCreateEvent.SpellDescriptionChanged(index, it)) },
                label = "Description",
                minLines = 2
            )
        }
    }
}

@Composable
fun DesktopCardSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onAddClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B241C)), // Dark forest green
        border = BorderStroke(1.dp, Color(0xFF2E7D32)), // Neon green border
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 18.sp)
                }
                if (onAddClick != null) {
                    IconButton(onClick = onAddClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color(0xFF4CAF50))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun AbilityScoreBox(
    label: String, 
    value: String, 
    color: Color,
    modifier: Modifier = Modifier, 
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(1.dp, color, RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White),
                modifier = Modifier.width(60.dp).padding(top = 4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = color,
                    cursorColor = color,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun CharacterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFF4CAF50).copy(alpha = 0.7f)) },
        modifier = modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4CAF50),
            unfocusedBorderColor = Color(0xFF2E7D32),
            cursorColor = Color(0xFF4CAF50),
            focusedContainerColor = Color.Black.copy(alpha = 0.2f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.2f)
        ),
        minLines = minLines,
        shape = RoundedCornerShape(8.dp),
        singleLine = minLines == 1
    )
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
            label = { Text(label, color = Color(0xFF4CAF50).copy(alpha = 0.7f)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF4CAF50),
                unfocusedBorderColor = Color(0xFF2E7D32),
                cursorColor = Color(0xFF4CAF50),
                focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White
            ),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            singleLine = true
        )
        if (options.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1B241C)).heightIn(max = 400.dp)
            ) {
                options.forEach { option ->
                    val text = optionLabel(option)
                    DropdownMenuItem(
                        text = { Text(text, color = Color.White) },
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
                        label = { Text(item, color = Color.White) },
                        trailingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp), tint = Color.White) },
                        colors = InputChipDefaults.inputChipColors(selectedContainerColor = Color(0xFF2E7D32))
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
                label = { Text("Add $label", color = Color(0xFF4CAF50).copy(alpha = 0.7f)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF4CAF50),
                    unfocusedBorderColor = Color(0xFF2E7D32),
                    cursorColor = Color(0xFF4CAF50),
                    focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                singleLine = true
            )
            
            if (filteredOptions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1B241C)).heightIn(max = 400.dp)
                ) {
                    filteredOptions.forEach { optionText ->
                        DropdownMenuItem(
                            text = { Text(optionText, color = Color.White) },
                            onClick = {
                                onAdd(optionText)
                                searchText = ""
                                expanded = false
                            }
                        )
                    }
                    if (searchText.isNotBlank() && !filteredOptions.contains(searchText)) {
                        DropdownMenuItem(
                            text = { Text("Add custom: \"$searchText\"", color = Color.White) },
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
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color(0xFF1B241C)).heightIn(max = 400.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("Add custom: \"$searchText\"", color = Color.White) },
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
