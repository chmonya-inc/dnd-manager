package com.dnd.helper.presentation.monstercreate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnd.helper.presentation.charactercreate.AbilityScoreBox
import com.dnd.helper.presentation.charactercreate.DesktopCardSection
import com.dnd.helper.presentation.charactercreate.DropdownMenuField
import com.dnd.helper.presentation.charactercreate.MultiSelectDropdownField
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterCreateScreen(
    onBackClick: () -> Unit,
    onMonsterCreated: () -> Unit,
) {
    val viewModelKey = remember { Random.nextLong().toString() }
    val viewModel: MonsterCreateViewModel = koinViewModel(key = viewModelKey)
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isSaved) {
        onMonsterCreated()
        return
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // COLUMN 1: Basic Info & Stats
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
                                        onClick = { viewModel.onEvent(MonsterCreateEvent.GenerateImage) },
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
                            onValueChange = { viewModel.onEvent(MonsterCreateEvent.AiPromptChanged(it)) },
                            label = { Text("AI Prompt (Auto-generated)") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.aiWidth.toString(),
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.AiSizeChanged(it.toIntOrNull() ?: 1024, state.aiHeight)) },
                                label = { Text("Width") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state.aiHeight.toString(),
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.AiSizeChanged(state.aiWidth, it.toIntOrNull() ?: 1024)) },
                                label = { Text("Height") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.onEvent(MonsterCreateEvent.NameChanged(it)) },
                            label = { Text("Monster Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(MonsterCreateEvent.DescriptionChanged(it)) },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DropdownMenuField(
                                label = "Alignment",
                                value = state.alignment,
                                options = state.availableAlignments,
                                optionLabel = { it.name },
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.AlignmentChanged(it)) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = state.challengeRating,
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.ChallengeRatingChanged(it)) },
                                label = { Text("CR") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.type,
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.TypeChanged(it)) },
                                label = { Text("Type") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.size,
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.SizeChanged(it)) },
                                label = { Text("Size") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Combat Stats", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.maxHp,
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.MaxHpChanged(it)) },
                                label = { Text("HP") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.armorClass,
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.ArmorClassChanged(it)) },
                                label = { Text("AC") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = state.speed,
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.SpeedChanged(it)) },
                                label = { Text("Speed") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Ability Scores", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            AbilityScoreBox("STR", state.strength, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.StrengthChanged(it)) }
                            Spacer(Modifier.width(4.dp))
                            AbilityScoreBox("DEX", state.dexterity, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.DexterityChanged(it)) }
                            Spacer(Modifier.width(4.dp))
                            AbilityScoreBox("CON", state.constitution, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.ConstitutionChanged(it)) }
                            Spacer(Modifier.width(4.dp))
                            AbilityScoreBox("INT", state.intelligence, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.IntelligenceChanged(it)) }
                            Spacer(Modifier.width(4.dp))
                            AbilityScoreBox("WIS", state.wisdom, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.WisdomChanged(it)) }
                            Spacer(Modifier.width(4.dp))
                            AbilityScoreBox("CHA", state.charisma, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.CharismaChanged(it)) }
                        }
                    }
                }
            }

            // COLUMN 2: Proficiencies and Multi-selects
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    DesktopCardSection(title = "Traits & Immunities", icon = Icons.Default.Build) {
                        MultiSelectDropdownField(
                            label = "Languages",
                            selectedItems = state.selectedLanguages,
                            options = state.availableLanguages,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddLanguage(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveLanguage(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        MultiSelectDropdownField(
                            label = "Condition Immunities",
                            selectedItems = state.selectedConditionImmunities,
                            options = state.availableConditions,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddConditionImmunity(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveConditionImmunity(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        MultiSelectDropdownField(
                            label = "Damage Immunities",
                            selectedItems = state.selectedDamageImmunities,
                            options = state.availableDamageTypes,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddDamageImmunity(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveDamageImmunity(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        MultiSelectDropdownField(
                            label = "Damage Resistances",
                            selectedItems = state.selectedDamageResistances,
                            options = state.availableDamageTypes,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddDamageResistance(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveDamageResistance(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        MultiSelectDropdownField(
                            label = "Damage Vulnerabilities",
                            selectedItems = state.selectedDamageVulnerabilities,
                            options = state.availableDamageTypes,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddDamageVulnerability(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveDamageVulnerability(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                item {
                    DesktopCardSection(title = "Abilities & Actions", icon = Icons.Default.List) {
                        MonsterActionEditor(
                            title = "Special Abilities",
                            actions = state.specialAbilities,
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddSpecialAbility(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveSpecialAbility(it)) }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        MonsterActionEditor(
                            title = "Actions",
                            actions = state.actions,
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddAction(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveAction(it)) }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        MonsterActionEditor(
                            title = "Legendary Actions",
                            actions = state.legendaryActions,
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddLegendaryAction(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveLegendaryAction(it)) }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        MonsterActionEditor(
                            title = "Reactions",
                            actions = state.reactions,
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddReaction(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveReaction(it)) }
                        )
                    }
                }
            }
        }

        // Bottom Bar
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBackClick) {
                    Text("Cancel")
                }
                
                if (state.error != null) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                }

                Button(
                    onClick = { viewModel.onEvent(MonsterCreateEvent.SaveMonster) },
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Monster")
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterActionEditor(
    title: String,
    actions: List<com.dnd.helper.domain.model.MonsterAction>,
    onAdd: (com.dnd.helper.domain.model.MonsterAction) -> Unit,
    onRemove: (com.dnd.helper.domain.model.MonsterAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        
        actions.forEach { action ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(action.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(action.description, fontSize = 12.sp)
                }
                IconButton(onClick = { onRemove(action) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    minLines = 2
                )
            }
            
            IconButton(
                onClick = { 
                    if (name.isNotBlank() && desc.isNotBlank()) {
                        onAdd(com.dnd.helper.domain.model.MonsterAction(name, desc))
                        name = ""
                        desc = ""
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}
