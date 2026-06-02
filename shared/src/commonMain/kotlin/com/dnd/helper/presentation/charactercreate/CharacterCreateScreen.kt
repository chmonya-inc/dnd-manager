package com.dnd.helper.presentation.charactercreate

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dnd.helper.domain.model.EquipmentSlot
import com.dnd.helper.domain.model.ItemRarity
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random

// Ability score colors
private val StrColor = Color(0xFFEF5350)
private val DexColor = Color(0xFF66BB6A)
private val ConColor = Color(0xFFFFA726)
private val IntColor = Color(0xFF42A5F5)
private val WisColor = Color(0xFFAB47BC)
private val ChaColor = Color(0xFFEC407A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCreateScreen(
    onBackClick: () -> Unit,
    onCharacterCreated: () -> Unit,
) {
    // Generate a unique key for each "session" of creating a character 
    // to ensure Koin/ViewModelStore gives us a fresh ViewModel every time.
    val viewModelKey = remember { Random.nextLong().toString() }
    val viewModel: CharacterCreateViewModel = koinViewModel(key = viewModelKey)
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // ===== Basic Info =====
            item { SectionHeader(icon = Icons.Default.Person, title = "Basic Info", accent = MaterialTheme.colorScheme.primary) }
            item {
                CardSection {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.NameChanged(it)) },
                        label = { Text("Character Name *") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.playerName,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.PlayerNameChanged(it)) },
                            label = { Text("Player") },
                            )
                        OutlinedTextField(
                            value = state.race,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.RaceChanged(it)) },
                            label = { Text("Race") },
                            )
                    }
                    Spacer(Modifier.height(6.dp))
                    TwoColumnRow {
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
                    }
                    Spacer(Modifier.height(6.dp))
                    TwoColumnRow {
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
                    Spacer(Modifier.height(6.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.experiencePoints,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ExperiencePointsChanged(it)) },
                            label = { Text("XP") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.imageUrl,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ImageUrlChanged(it)) },
                            label = { Text("Image URL") },
                            )
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.DescriptionChanged(it)) },
                        label = { Text("Bio / Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ===== Appearance =====
            item { SectionHeader(icon = Icons.Default.Face, title = "Appearance", accent = MaterialTheme.colorScheme.secondary) }
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
                    Spacer(Modifier.height(6.dp))
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
                    Spacer(Modifier.height(6.dp))
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
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.skin,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkinChanged(it)) },
                        label = { Text("Skin") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ===== Ability Scores =====
            item { SectionHeader(icon = Icons.Default.FitnessCenter, title = "Ability Scores", accent = StrColor) }
            item {
                CardSection {
                    TwoColumnRow {
                        StatCard("STR", state.strength, StrColor) { viewModel.onEvent(CharacterCreateEvent.StrengthChanged(it)) }
                        StatCard("DEX", state.dexterity, DexColor) { viewModel.onEvent(CharacterCreateEvent.DexterityChanged(it)) }
                    }
                    Spacer(Modifier.height(6.dp))
                    TwoColumnRow {
                        StatCard("CON", state.constitution, ConColor) { viewModel.onEvent(CharacterCreateEvent.ConstitutionChanged(it)) }
                        StatCard("INT", state.intelligence, IntColor) { viewModel.onEvent(CharacterCreateEvent.IntelligenceChanged(it)) }
                    }
                    Spacer(Modifier.height(6.dp))
                    TwoColumnRow {
                        StatCard("WIS", state.wisdom, WisColor) { viewModel.onEvent(CharacterCreateEvent.WisdomChanged(it)) }
                        StatCard("CHA", state.charisma, ChaColor) { viewModel.onEvent(CharacterCreateEvent.CharismaChanged(it)) }
                    }
                }
            }

            // ===== HP & Combat =====
            item { SectionHeader(icon = Icons.Default.Shield, title = "Combat", accent = ConColor) }
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
                    Spacer(Modifier.height(6.dp))
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
                            label = { Text("AC") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                    }
                    Spacer(Modifier.height(6.dp))
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
                            label = { Text("Speed") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                    }
                    Spacer(Modifier.height(6.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.proficiencyBonus,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ProficiencyBonusChanged(it)) },
                            label = { Text("Prof. Bonus") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.hitDice,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.HitDiceChanged(it)) },
                            label = { Text("Hit Dice") },
                            )
                    }
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.hitDiceCurrent,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.HitDiceCurrentChanged(it)) },
                        label = { Text("Hit Dice Left") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ===== Status =====
            item { SectionHeader(icon = Icons.Default.HealthAndSafety, title = "Status", accent = WisColor) }
            item {
                CardSection {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Inspiration", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = state.inspiration,
                            onCheckedChange = { viewModel.onEvent(CharacterCreateEvent.InspirationChanged(it)) },
                        )
                    }
                    Spacer(Modifier.height(6.dp))
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
                            label = { Text("DS Successes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                    }
                    Spacer(Modifier.height(6.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.deathSaveFailures,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.DeathSaveFailuresChanged(it)) },
                            label = { Text("DS Failures") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = state.conditions,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.ConditionsChanged(it)) },
                            label = { Text("Conditions") },
                            )
                    }
                }
            }

            // ===== Proficiencies =====
            item { SectionHeader(icon = Icons.Default.School, title = "Proficiencies", accent = IntColor) }
            item {
                CardSection {
                    TwoColumnRow {
                        OutlinedTextField(
                            value = state.savingThrows,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SavingThrowsChanged(it)) },
                            label = { Text("Saving Throws") },
                            )
                        OutlinedTextField(
                            value = state.skills,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillsChanged(it)) },
                            label = { Text("Skills") },
                            )
                    }
                    Spacer(Modifier.height(6.dp))
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
                    Spacer(Modifier.height(6.dp))
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
            item { SectionHeader(icon = Icons.Default.ShoppingBag, title = "Items", accent = MaterialTheme.colorScheme.tertiary) }
            itemsIndexed(state.items) { index, item ->
                CardSection {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Item #${index + 1}", style = MaterialTheme.typography.labelLarge)
                        IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.RemoveItem(index)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
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
                    TwoColumnRow {
                        CompactSlotDropdown(
                            selected = item.slot,
                            onSelect = { viewModel.onEvent(CharacterCreateEvent.ItemSlotChanged(index, it)) },
                            )
                        CompactRarityDropdown(
                            selected = item.rarity,
                            onSelect = { viewModel.onEvent(CharacterCreateEvent.ItemRarityChanged(index, it)) },
                            )
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = item.description,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ItemDescriptionChanged(index, it)) },
                        label = { Text("Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = item.imageUrl ?: "",
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ItemImageUrlChanged(index, it)) },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = item.equipped,
                            onCheckedChange = { viewModel.onEvent(CharacterCreateEvent.ItemEquippedChanged(index, it)) },
                        )
                        Text("Equipped", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Button(
                    onClick = { viewModel.onEvent(CharacterCreateEvent.AddItem) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item")
                }
            }

            // ===== Weapons =====
            item { SectionHeader(icon = Icons.Default.SportsMartialArts, title = "Weapons", accent = StrColor) }
            itemsIndexed(state.weapons) { index, weapon ->
                CardSection {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Weapon #${index + 1}", style = MaterialTheme.typography.labelLarge)
                        IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.RemoveWeapon(index)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
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
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Weapon")
                }
            }

            // ===== Skills =====
            item { SectionHeader(icon = Icons.Default.AutoFixHigh, title = "Skills & Spells", accent = ChaColor) }
            itemsIndexed(state.skillList) { index, skill ->
                CardSection {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Skill #${index + 1}", style = MaterialTheme.typography.labelLarge)
                        IconButton(onClick = { viewModel.onEvent(CharacterCreateEvent.RemoveSkill(index)) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remove")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = skill.name,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillNameChanged(index, it)) },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(4.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = skill.damage,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillDamageChanged(index, it)) },
                            label = { Text("Damage") },
                            )
                        OutlinedTextField(
                            value = skill.damageType,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillDamageTypeChanged(index, it)) },
                            label = { Text("Damage Type") },
                            )
                    }
                    Spacer(Modifier.height(4.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = skill.resourceCost,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillResourceCostChanged(index, it)) },
                            label = { Text("Cost (e.g. 1 Action)") },
                            )
                        OutlinedTextField(
                            value = skill.range,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillRangeChanged(index, it)) },
                            label = { Text("Range") },
                            )
                    }
                    Spacer(Modifier.height(4.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = skill.castingTime,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillCastingTimeChanged(index, it)) },
                            label = { Text("Casting Time") },
                            )
                        OutlinedTextField(
                            value = skill.duration,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillDurationChanged(index, it)) },
                            label = { Text("Duration") },
                            )
                    }
                    Spacer(Modifier.height(4.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = skill.level.toString(),
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillLevelChanged(index, it)) },
                            label = { Text("Level") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        OutlinedTextField(
                            value = skill.school,
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillSchoolChanged(index, it)) },
                            label = { Text("School") },
                            )
                    }
                    Spacer(Modifier.height(4.dp))
                    TwoColumnRow {
                        OutlinedTextField(
                            value = skill.iconUrl ?: "",
                            onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillIconNameChanged(index, it)) },
                            label = { Text("Icon URL") },
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = skill.isPassive,
                                onCheckedChange = { viewModel.onEvent(CharacterCreateEvent.SkillIsPassiveChanged(index, it)) },
                            )
                            Text("Passive", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = skill.description,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.SkillDescriptionChanged(index, it)) },
                        label = { Text("Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                Button(
                    onClick = { viewModel.onEvent(CharacterCreateEvent.AddSkill) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Skill")
                }
            }

            // ===== Features =====
            item { SectionHeader(icon = Icons.Default.Star, title = "Features & Traits", accent = MaterialTheme.colorScheme.secondary) }
            item {
                CardSection {
                    OutlinedTextField(
                        value = state.classFeatures,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.ClassFeaturesChanged(it)) },
                        label = { Text("Class Features (one per line)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.racialTraits,
                        onValueChange = { viewModel.onEvent(CharacterCreateEvent.RacialTraitsChanged(it)) },
                        label = { Text("Racial Traits (one per line)") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
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
                            modifier = Modifier.padding(end = 8.dp).size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
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

// ===== Helper Composables =====

@Composable
private fun SectionHeader(icon: ImageVector, title: String, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = accent,
                fontWeight = FontWeight.Bold,
            )
        }
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(accent.copy(alpha = 0.2f)),
        )
    }
}

@Composable
private fun CardSection(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
        ),
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
private fun StatCard(label: String, value: String, color: Color, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(2.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                textAlign = TextAlign.Center,
                color = color,
                fontWeight = FontWeight.Bold,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactSlotDropdown(selected: EquipmentSlot?, onSelect: (EquipmentSlot?) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(null) + EquipmentSlot.entries
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "None",
            onValueChange = {},
            readOnly = true,
            label = { Text("Slot", style = MaterialTheme.typography.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "None") },
                    onClick = { onSelect(option); expanded = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompactRarityDropdown(selected: ItemRarity, onSelect: (ItemRarity) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected.name.lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text("Rarity", style = MaterialTheme.typography.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ItemRarity.entries.forEach { rarity ->
                DropdownMenuItem(
                    text = { Text(rarity.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = { onSelect(rarity); expanded = false },
                )
            }
        }
    }
}
