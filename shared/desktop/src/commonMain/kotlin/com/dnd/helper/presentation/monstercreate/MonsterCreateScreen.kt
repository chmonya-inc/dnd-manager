package com.dnd.helper.presentation.monstercreate

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import com.dnd.helper.data.remote.dto.common.DamageDto
import com.dnd.helper.data.remote.dto.common.DcDto
import com.dnd.helper.data.remote.dto.monster.*
import com.dnd.helper.presentation.charactercreate.DropdownMenuField
import com.dnd.helper.presentation.charactercreate.MultiSelectDropdownField
import com.dnd.helper.theme.dndColors
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterCreateScreen(
    onBackClick: () -> Unit,
    onMonsterCreated: () -> Unit,
    initialMonster: com.dnd.helper.domain.model.Monster? = null
) {
    val viewModelKey = remember { Random.nextLong().toString() }
    val viewModel: MonsterCreateViewModel = koinViewModel(key = viewModelKey)
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(initialMonster) {
        initialMonster?.let { viewModel.onEvent(MonsterCreateEvent.LoadMonster(it)) }
    }

    var showActionDialog by remember { mutableStateOf(false) }
    var actionTypeToEdit by remember { mutableStateOf("Action") }

    if (state.isSaved) {
        onMonsterCreated()
        return
    }

    Scaffold(
        containerColor = Color(0xFF0A0F0A), // Very dark green background
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
                    if (state.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = Color(0xFF4CAF50))
                    } else {
                        Button(
                            onClick = { viewModel.onEvent(MonsterCreateEvent.SaveMonster) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Text(if (initialMonster == null) "Create Monster" else "Update Monster")
                        }
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
            // COLUMN 1: Basic Info & Image (Weight 1)
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MonsterDesktopCardSection(title = "Basic Info", icon = Icons.Default.Person) {
                        // Image Generator Area - Top Left integrated
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                                    .background(Color.Black),
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
                                    IconButton(
                                        onClick = { viewModel.onEvent(MonsterCreateEvent.GenerateImage) },
                                        modifier = Modifier.background(Color(0xFF2E7D32).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = "Generate", tint = Color.White)
                                    }
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                MonsterTextField(
                                    value = state.name,
                                    onValueChange = { viewModel.onEvent(MonsterCreateEvent.NameChanged(it)) },
                                    label = "Monster Name"
                                )
                                MonsterTextField(
                                    value = state.challengeRating,
                                    onValueChange = { viewModel.onEvent(MonsterCreateEvent.ChallengeRatingChanged(it)) },
                                    label = "CR"
                                )
                                MonsterTextField(
                                    value = state.imageUrl,
                                    onValueChange = { viewModel.onEvent(MonsterCreateEvent.ImageUrlChanged(it)) },
                                    label = "Image URL"
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DropdownMenuField(
                                label = "Type",
                                value = state.type,
                                options = listOf("Humanoid", "Beast", "Undead", "Fiend", "Dragon", "Construct").map { ApiReferenceDto(name = it) },
                                optionLabel = { it.name },
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.TypeChanged(it)) },
                                modifier = Modifier.weight(1f)
                            )
                            DropdownMenuField(
                                label = "Size",
                                value = state.size,
                                options = listOf("Tiny", "Small", "Medium", "Large", "Huge", "Gargantuan").map { ApiReferenceDto(name = it) },
                                optionLabel = { it.name },
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.SizeChanged(it)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        
                        DropdownMenuField(
                            label = "Alignment",
                            value = state.alignment,
                            options = state.availableAlignments,
                            optionLabel = { it.name },
                            onValueChange = { viewModel.onEvent(MonsterCreateEvent.AlignmentChanged(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(Modifier.height(8.dp))
                        
                        MonsterTextField(
                            value = state.description,
                            onValueChange = { viewModel.onEvent(MonsterCreateEvent.DescriptionChanged(it)) },
                            label = "Background / Description",
                            minLines = 3
                        )

                        Spacer(Modifier.height(16.dp))
                        Text("AI Generation Settings", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        
                        MonsterTextField(
                            value = state.aiPrompt,
                            onValueChange = { viewModel.onEvent(MonsterCreateEvent.AiPromptChanged(it)) },
                            label = "AI Prompt",
                            minLines = 2
                        )
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MonsterTextField(
                                value = state.aiWidth.toString(),
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.AiSizeChanged(it.toIntOrNull() ?: 1024, state.aiHeight)) },
                                label = "Width",
                                modifier = Modifier.weight(1f)
                            )
                            MonsterTextField(
                                value = state.aiHeight.toString(),
                                onValueChange = { viewModel.onEvent(MonsterCreateEvent.AiSizeChanged(state.aiWidth, it.toIntOrNull() ?: 1024)) },
                                label = "Height",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // COLUMN 2: Stats & Proficiencies (Weight 1.2)
            LazyColumn(
                modifier = Modifier.weight(1.2f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MonsterDesktopCardSection(title = "Ability Scores", icon = Icons.Default.BarChart) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                MonsterAbilityScoreBox("STR", state.strength, dndColors.strength, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.StrengthChanged(it)) }
                                MonsterAbilityScoreBox("DEX", state.dexterity, dndColors.dexterity, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.DexterityChanged(it)) }
                                MonsterAbilityScoreBox("CON", state.constitution, dndColors.constitution, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.ConstitutionChanged(it)) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                                MonsterAbilityScoreBox("INT", state.intelligence, dndColors.intelligence, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.IntelligenceChanged(it)) }
                                MonsterAbilityScoreBox("WIS", state.wisdom, dndColors.wisdom, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.WisdomChanged(it)) }
                                MonsterAbilityScoreBox("CHA", state.charisma, dndColors.charisma, Modifier.weight(1f)) { viewModel.onEvent(MonsterCreateEvent.CharismaChanged(it)) }
                            }
                        }
                    }
                }
                
                item {
                    MonsterDesktopCardSection(title = "Combat Stats") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MonsterTextField(value = state.maxHp, onValueChange = { viewModel.onEvent(MonsterCreateEvent.MaxHpChanged(it)) }, label = "HP", modifier = Modifier.weight(1f))
                            MonsterTextField(value = state.armorClass, onValueChange = { viewModel.onEvent(MonsterCreateEvent.ArmorClassChanged(it)) }, label = "AC", modifier = Modifier.weight(1f))
                            MonsterTextField(value = state.speed, onValueChange = { viewModel.onEvent(MonsterCreateEvent.SpeedChanged(it)) }, label = "Speed", modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(8.dp))
                        MonsterTextField(value = state.hitDice, onValueChange = { viewModel.onEvent(MonsterCreateEvent.HitDiceChanged(it)) }, label = "Hit Die (e.g. 2d8+4)")
                    }
                }
                
                item {
                    MonsterDesktopCardSection(title = "Immunities & Resistances") {
                        MultiSelectDropdownField(
                            label = "Condition Immunities",
                            selectedItems = state.selectedConditionImmunities,
                            options = state.availableConditions,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddConditionImmunity(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveConditionImmunity(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        MultiSelectDropdownField(
                            label = "Damage Immunities",
                            selectedItems = state.selectedDamageImmunities,
                            options = state.availableDamageTypes,
                            optionLabel = { it.name },
                            onAdd = { viewModel.onEvent(MonsterCreateEvent.AddDamageImmunity(it)) },
                            onRemove = { viewModel.onEvent(MonsterCreateEvent.RemoveDamageImmunity(it)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // COLUMN 3: Actions & Features (Weight 1.5)
            LazyColumn(
                modifier = Modifier.weight(1.5f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    MonsterDesktopCardSection(
                        title = "Special Abilities", 
                        icon = Icons.Default.Star, 
                        onAddClick = {
                            actionTypeToEdit = "Special Ability"
                            showActionDialog = true
                        }
                    ) {
                        state.specialAbilities.forEach { ability ->
                            MonsterActionRichItem(ability.name, ability.desc) { viewModel.onEvent(MonsterCreateEvent.RemoveSpecialAbility(ability)) }
                        }
                    }
                }

                item {
                    MonsterDesktopCardSection(
                        title = "Actions", 
                        icon = Icons.Default.Gavel, 
                        onAddClick = {
                            actionTypeToEdit = "Action"
                            showActionDialog = true
                        }
                    ) {
                        state.actions.forEach { action ->
                            MonsterActionRichItem(action.name, action.desc) { viewModel.onEvent(MonsterCreateEvent.RemoveAction(action)) }
                        }
                    }
                }
                
                item {
                    MonsterDesktopCardSection(
                        title = "Legendary Actions", 
                        icon = Icons.Default.EmojiEvents, 
                        onAddClick = {
                            actionTypeToEdit = "Legendary Action"
                            showActionDialog = true
                        }
                    ) {
                        state.legendaryActions.forEach { action ->
                            MonsterActionRichItem(action.name, action.desc) { viewModel.onEvent(MonsterCreateEvent.RemoveLegendaryAction(action)) }
                        }
                    }
                }
            }
        }
    }

    if (showActionDialog) {
        MonsterActionRichDialog(
            type = actionTypeToEdit,
            onDismiss = { showActionDialog = false },
            onConfirm = { actionData ->
                when (actionTypeToEdit) {
                    "Special Ability" -> viewModel.onEvent(MonsterCreateEvent.AddSpecialAbility(actionData.toSpecialAbility()))
                    "Action" -> viewModel.onEvent(MonsterCreateEvent.AddAction(actionData.toAction()))
                    "Legendary Action" -> viewModel.onEvent(MonsterCreateEvent.AddLegendaryAction(actionData.toAction()))
                    "Reaction" -> viewModel.onEvent(MonsterCreateEvent.AddReaction(actionData.toAction()))
                }
                showActionDialog = false
            }
        )
    }
}

@Composable
fun MonsterDesktopCardSection(
    title: String,
    icon: ImageVector? = null,
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
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun MonsterAbilityScoreBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = modifier
            .border(2.dp, color, RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(label, fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
        
        // Main editable score - using BasicTextField to remove default Material indicator and padding
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(60.dp).padding(vertical = 2.dp),
            textStyle = TextStyle(
                fontSize = 24.sp, 
                fontWeight = FontWeight.Bold, 
                color = Color.White, 
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(color)
        )

        val modStr = try {
            val score = value.toIntOrNull() ?: 10
            val mod = (score - 10) / 2
            if (mod >= 0) "+$mod" else mod.toString()
        } catch (_: Exception) { "+0" }
        
        // Calculated modifier
        Text(
            text = "($modStr)", 
            fontWeight = FontWeight.Medium, 
            color = Color.Gray, 
            fontSize = 14.sp
        )
    }
}

@Composable
fun MonsterTextField(
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
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
fun MonsterActionRichItem(
    name: String,
    desc: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 14.sp)
            Text(desc, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 3)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFD32F2F).copy(alpha = 0.8f))
        }
    }
}

// Data structure for dialog
data class ActionDialogData(
    val name: String = "",
    val desc: String = "",
    val attackBonus: String = "",
    val damageDice: String = "",
    val damageType: String = "",
    val dcValue: String = "",
    val dcType: String = "STR"
) {
    fun toAction() = MonsterActionDto(
        name = name,
        desc = desc,
        attack_bonus = attackBonus.toIntOrNull(),
        dc = if (dcValue.isNotBlank()) DcDto(dc_type = ApiReferenceDto(name = dcType), dc_value = dcValue.toDoubleOrNull()) else null,
        damage = if (damageDice.isNotBlank()) listOf(DamageDto(damage_dice = damageDice, damage_type = ApiReferenceDto(name = damageType))) else emptyList()
    )

    fun toSpecialAbility() = MonsterSpecialAbilityDto(
        name = name,
        desc = desc,
        attack_bonus = attackBonus.toIntOrNull(),
        dc = if (dcValue.isNotBlank()) DcDto(dc_type = ApiReferenceDto(name = dcType), dc_value = dcValue.toDoubleOrNull()) else null,
        damage = if (damageDice.isNotBlank()) listOf(DamageDto(damage_dice = damageDice, damage_type = ApiReferenceDto(name = damageType))) else emptyList()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonsterActionRichDialog(
    type: String,
    onDismiss: () -> Unit,
    onConfirm: (ActionDialogData) -> Unit
) {
    var data by remember { mutableStateOf(ActionDialogData()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B241C)),
            border = BorderStroke(1.dp, Color(0xFF4CAF50)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add $type", fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontSize = 22.sp)
                
                MonsterTextField(value = data.name, onValueChange = { data = data.copy(name = it) }, label = "Name")
                MonsterTextField(value = data.desc, onValueChange = { data = data.copy(desc = it) }, label = "Description", minLines = 3)
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonsterTextField(
                        value = data.attackBonus, 
                        onValueChange = { data = data.copy(attackBonus = it) }, 
                        label = "Atk Bonus", 
                        modifier = Modifier.weight(1f)
                    )
                    MonsterTextField(
                        value = data.damageDice, 
                        onValueChange = { data = data.copy(damageDice = it) }, 
                        label = "Dmg Dice", 
                        modifier = Modifier.weight(1.5f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonsterTextField(
                        value = data.damageType, 
                        onValueChange = { data = data.copy(damageType = it) }, 
                        label = "Dmg Type", 
                        modifier = Modifier.weight(1f)
                    )
                    MonsterTextField(
                        value = data.dcValue, 
                        onValueChange = { data = data.copy(dcValue = it) }, 
                        label = "DC Value", 
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
                    Button(
                        onClick = { if (data.name.isNotBlank()) onConfirm(data) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        enabled = data.name.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}
