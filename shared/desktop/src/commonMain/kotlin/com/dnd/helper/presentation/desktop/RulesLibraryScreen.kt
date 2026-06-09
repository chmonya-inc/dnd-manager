package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesLibraryScreen(
    viewModel: RulesLibraryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Sidebar for categories
        NavigationRail(
            modifier = Modifier.width(120.dp).fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        ) {
            RuleCategory.entries.forEach { category ->
                NavigationRailItem(
                    selected = state.selectedCategory == category,
                    onClick = { viewModel.setCategory(category) },
                    icon = {
                        Icon(
                            imageVector = when(category) {
                                RuleCategory.CharacterData -> Icons.Default.Person
                                RuleCategory.Spells -> Icons.Default.AutoFixHigh
                                RuleCategory.Equipment -> Icons.Default.Shield
                                RuleCategory.Monsters -> Icons.Default.Pets
                                RuleCategory.Mechanics -> Icons.Default.Build
                                RuleCategory.Rules -> Icons.Default.MenuBook
                            },
                            contentDescription = category.name
                        )
                    },
                    label = { Text(category.name, maxLines = 1) }
                )
            }
        }

        VerticalDivider()

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            CategoryDataFragment(state, state.selectedCategory)
            
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            FloatingActionButton(
                onClick = { viewModel.reload() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload"
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDataFragment(state: RulesLibraryState, category: RuleCategory) {
    val categoryItems: List<Pair<String, List<Any>>> = remember(state, category) {
        when(category) {
            RuleCategory.CharacterData -> listOf(
                "Classes" to state.classes,
                "Races" to state.races,
                "Subraces" to state.subraces,
                "Subclasses" to state.subclasses,
                "Feats" to state.feats,
                "Traits" to state.traits,
                "Features" to state.features,
                "Backgrounds" to state.backgrounds,
                "Skills" to state.skills,
                "Ability Scores" to state.abilityScores,
                "Alignments" to state.alignments,
                "Languages" to state.languages,
                "Proficiencies" to state.proficiencies
            )
            RuleCategory.Spells -> listOf(
                "Spells" to state.spells,
                "Magic Schools" to state.magicSchools
            )
            RuleCategory.Equipment -> listOf(
                "Equipment Categories" to state.equipmentCategories,
                "Magic Items" to state.magicItems,
                "Weapon Properties" to state.weaponProperties
            )
            RuleCategory.Monsters -> listOf(
                "Monsters" to state.monsters
            )
            RuleCategory.Mechanics -> listOf(
                "Conditions" to state.conditions,
                "Damage Types" to state.damageTypes
            )
            RuleCategory.Rules -> listOf(
                "Rules" to state.rules,
                "Rule Sections" to state.ruleSections
            )
        }
    }

    var selectedType by androidx.compose.runtime.saveable.rememberSaveable(categoryItems) { 
        mutableStateOf(categoryItems.firstOrNull()?.first ?: "") 
    }

    // fallback if selection becomes invalid due to data loading
    LaunchedEffect(categoryItems) {
        if (categoryItems.isNotEmpty() && categoryItems.none { it.first == selectedType }) {
            selectedType = categoryItems.first().first
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Secondary sidebar
        LazyColumn(
            modifier = Modifier.width(200.dp).fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryItems) { (type, list) ->
                val isSelected = selectedType == type
                Surface(
                    onClick = { selectedType = type },
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        Badge(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                        ) {
                            Text(list.size.toString())
                        }
                    }
                }
            }
        }

        VerticalDivider()

        // List of items
        val currentList = categoryItems.find { it.first == selectedType }?.second ?: emptyList<Any>()
        
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentList, key = { getDtoKey(it) }) { item ->
                ExpandableDtoCard(item)
            }
        }
    }
}

@Composable
fun ExpandableDtoCard(item: Any) {
    var expanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getDtoTitle(item),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RenderDtoDetails(item)
                }
            }
        }
    }
}

private fun getDtoTitle(item: Any): String {
    return when (item) {
        is com.dnd.helper.data.remote.dto.character.ClassDto -> item.name
        is com.dnd.helper.data.remote.dto.character.RaceDto -> item.name
        is com.dnd.helper.data.remote.dto.character.SubraceDto -> item.name
        is com.dnd.helper.data.remote.dto.character.SubclassDto -> item.name
        is com.dnd.helper.data.remote.dto.character.FeatDto -> item.name
        is com.dnd.helper.data.remote.dto.character.TraitDto -> item.name
        is com.dnd.helper.data.remote.dto.character.FeatureDto -> item.name
        is com.dnd.helper.data.remote.dto.character.BackgroundDto -> item.name
        is com.dnd.helper.data.remote.dto.character.DndSkillDto -> item.name
        is com.dnd.helper.data.remote.dto.character.AbilityScoreDto -> item.full_name
        is com.dnd.helper.data.remote.dto.character.AlignmentDto -> item.name
        is com.dnd.helper.data.remote.dto.character.LanguageDto -> item.name
        is com.dnd.helper.data.remote.dto.character.ProficiencyDto -> item.name
        is com.dnd.helper.data.remote.dto.spell.SpellDto -> item.name
        is com.dnd.helper.data.remote.dto.spell.MagicSchoolDto -> item.name
        is com.dnd.helper.data.remote.dto.equipment.EquipmentCategoryDto -> item.name
        is com.dnd.helper.data.remote.dto.equipment.MagicItemDto -> item.name
        is com.dnd.helper.data.remote.dto.equipment.WeaponPropertyDto -> item.name
        is com.dnd.helper.data.remote.dto.monster.MonsterDto -> item.name
        is com.dnd.helper.data.remote.dto.game.ConditionDto -> item.name
        is com.dnd.helper.data.remote.dto.game.DamageTypeDto -> item.name
        is com.dnd.helper.data.remote.dto.game.RuleDto -> item.name
        is com.dnd.helper.data.remote.dto.game.RuleSectionDto -> item.name
        else -> "Unknown"
    }
}

private fun getDtoKey(item: Any): String {
    return when (item) {
        is com.dnd.helper.data.remote.dto.character.ClassDto -> "class_${item.index}"
        is com.dnd.helper.data.remote.dto.character.RaceDto -> "race_${item.index}"
        is com.dnd.helper.data.remote.dto.character.SubraceDto -> "subrace_${item.index}"
        is com.dnd.helper.data.remote.dto.character.SubclassDto -> "subclass_${item.index}"
        is com.dnd.helper.data.remote.dto.character.FeatDto -> "feat_${item.index}"
        is com.dnd.helper.data.remote.dto.character.TraitDto -> "trait_${item.index}"
        is com.dnd.helper.data.remote.dto.character.FeatureDto -> "feature_${item.index}"
        is com.dnd.helper.data.remote.dto.character.BackgroundDto -> "background_${item.index}"
        is com.dnd.helper.data.remote.dto.character.DndSkillDto -> "skill_${item.index}"
        is com.dnd.helper.data.remote.dto.character.AbilityScoreDto -> "ability_${item.index}"
        is com.dnd.helper.data.remote.dto.character.AlignmentDto -> "alignment_${item.index}"
        is com.dnd.helper.data.remote.dto.character.LanguageDto -> "language_${item.index}"
        is com.dnd.helper.data.remote.dto.character.ProficiencyDto -> "proficiency_${item.index}"
        is com.dnd.helper.data.remote.dto.spell.SpellDto -> "spell_${item.index}"
        is com.dnd.helper.data.remote.dto.spell.MagicSchoolDto -> "school_${item.index}"
        is com.dnd.helper.data.remote.dto.equipment.EquipmentCategoryDto -> "equipcat_${item.index}"
        is com.dnd.helper.data.remote.dto.equipment.MagicItemDto -> "magicitem_${item.index}"
        is com.dnd.helper.data.remote.dto.equipment.WeaponPropertyDto -> "weaponprop_${item.index}"
        is com.dnd.helper.data.remote.dto.monster.MonsterDto -> "monster_${item.index}"
        is com.dnd.helper.data.remote.dto.game.ConditionDto -> "condition_${item.index}"
        is com.dnd.helper.data.remote.dto.game.DamageTypeDto -> "damagetype_${item.index}"
        is com.dnd.helper.data.remote.dto.game.RuleDto -> "rule_${item.index}"
        is com.dnd.helper.data.remote.dto.game.RuleSectionDto -> "rulesection_${item.index}"
        else -> item.hashCode().toString()
    }
}

@Composable
private fun RenderDtoDetails(item: Any) {
    when (item) {
        is com.dnd.helper.data.remote.dto.character.ClassDto -> {
            DtoField("Hit Die", "d${item.hit_die}")
            DtoField("Proficiencies", item.proficiencies.joinToString { it.name })
            DtoField("Saving Throws", item.saving_throws.joinToString { it.name })
            DtoField("Subclasses", item.subclasses.joinToString { it.name })
            if (item.starting_equipment.isNotEmpty()) {
                DtoField("Starting Equipment", item.starting_equipment.joinToString { "${it.quantity}x ${it.equipment.name}" })
            }
            val spellcasting = item.spellcasting
            if (spellcasting != null) {
                DtoField("Spellcasting Ability", spellcasting.spellcasting_ability.name)
            }
        }
        is com.dnd.helper.data.remote.dto.character.RaceDto -> {
            DtoField("Speed", "${item.speed} ft.")
            DtoField("Alignment", item.alignment)
            DtoField("Size", "${item.size} - ${item.size_description}")
            DtoField("Ability Bonuses", item.ability_bonuses.joinToString { "${it.ability_score.name} +${it.bonus}" })
            DtoField("Languages", item.language_desc)
            DtoField("Traits", item.traits.joinToString { it.name })
            if (item.subraces.isNotEmpty()) DtoField("Subraces", item.subraces.joinToString { it.name })
        }
        is com.dnd.helper.data.remote.dto.character.SubraceDto -> {
            DtoField("Parent Race", item.race.name)
            DtoField("Description", item.desc)
            DtoField("Ability Bonuses", item.ability_bonuses.joinToString { "${it.ability_score.name} +${it.bonus}" })
            DtoField("Racial Traits", item.racial_traits.joinToString { it.name })
        }
        is com.dnd.helper.data.remote.dto.character.SubclassDto -> {
            DtoField("Parent Class", item.`class`.name)
            DtoField("Flavor", item.subclass_flavor)
            DtoField("Description", item.desc.joinToString("\n"))
        }
        is com.dnd.helper.data.remote.dto.character.FeatDto -> {
            DtoField("Description", item.desc.joinToString("\n"))
            if (item.prerequisites.isNotEmpty()) {
                DtoField("Prerequisites", item.prerequisites.joinToString { req ->
                    val name = req.ability_score?.name ?: ""
                    val score = req.minimum_score?.toInt()?.toString() ?: ""
                    if (name.isNotEmpty() && score.isNotEmpty()) "$name $score" else "Other"
                })
            }
        }
        is com.dnd.helper.data.remote.dto.character.TraitDto -> {
            DtoField("Description", item.desc.joinToString("\n"))
            if (item.races.isNotEmpty()) DtoField("Races", item.races.joinToString { it.name })
            if (item.subraces.isNotEmpty()) DtoField("Subraces", item.subraces.joinToString { it.name })
        }
        is com.dnd.helper.data.remote.dto.character.FeatureDto -> {
            DtoField("Level", item.level.toString())
            DtoField("Class", item.`class`.name)
            if (item.subclass != null) DtoField("Subclass", item.subclass!!.name)
            DtoField("Description", item.desc.joinToString("\n"))
            if (item.prerequisites.isNotEmpty()) {
                DtoField("Prerequisites", item.prerequisites.joinToString { req ->
                    req.feature ?: req.spell ?: "Level ${req.level ?: "?"}"
                })
            }
        }
        is com.dnd.helper.data.remote.dto.character.BackgroundDto -> {
            DtoField("Starting Proficiencies", item.starting_proficiencies.joinToString { it.name })
            DtoField("Feature", item.feature.name)
            if (item.feature.desc.isNotEmpty()) {
                DtoField("Feature Description", item.feature.desc.joinToString("\n"))
            }
            if (item.starting_equipment.isNotEmpty()) {
                DtoField("Starting Equipment", item.starting_equipment.joinToString { "${it.quantity}x ${it.equipment.name}" })
            }
        }
        is com.dnd.helper.data.remote.dto.character.DndSkillDto -> {
            DtoField("Ability Score", item.ability_score.name)
            DtoField("Description", item.desc.joinToString("\n"))
        }
        is com.dnd.helper.data.remote.dto.character.AbilityScoreDto -> {
            DtoField("Abbreviation", item.name)
            DtoField("Description", item.desc.joinToString("\n"))
            DtoField("Skills", item.skills.joinToString { it.name })
        }
        is com.dnd.helper.data.remote.dto.character.AlignmentDto -> {
            DtoField("Abbreviation", item.abbreviation)
            DtoField("Description", item.desc)
        }
        is com.dnd.helper.data.remote.dto.character.LanguageDto -> {
            DtoField("Type", item.type)
            if (item.script != null) DtoField("Script", item.script!!)
            if (!item.desc.isNullOrBlank()) DtoField("Description", item.desc!!)
            if (item.typical_speakers.isNotEmpty()) DtoField("Typical Speakers", item.typical_speakers.joinToString())
        }
        is com.dnd.helper.data.remote.dto.character.ProficiencyDto -> {
            DtoField("Type", item.type)
            if (item.classes.isNotEmpty()) DtoField("Classes", item.classes.joinToString { it.name })
            if (item.races.isNotEmpty()) DtoField("Races", item.races.joinToString { it.name })
        }
        is com.dnd.helper.data.remote.dto.spell.SpellDto -> {
            DtoField("Level", if (item.level == 0) "Cantrip" else item.level.toString())
            DtoField("School", item.school.name)
            DtoField("Casting Time", item.casting_time)
            DtoField("Range", item.range)
            DtoField("Components", item.components.joinToString() + if (item.material != null) " (${item.material})" else "")
            DtoField("Duration", item.duration + if (item.concentration) " (Concentration)" else "")
            DtoField("Classes", item.classes.joinToString { it.name })
            DtoField("Description", item.desc.joinToString("\n"))
            if (item.higher_level.isNotEmpty()) DtoField("At Higher Levels", item.higher_level.joinToString("\n"))
        }
        is com.dnd.helper.data.remote.dto.spell.MagicSchoolDto -> {
            DtoField("Description", item.desc)
        }
        is com.dnd.helper.data.remote.dto.equipment.EquipmentCategoryDto -> {
            DtoField("Equipment Count", item.equipment.size.toString())
            DtoField("Items", item.equipment.take(10).joinToString { it.name } + if (item.equipment.size > 10) "..." else "")
        }
        is com.dnd.helper.data.remote.dto.equipment.MagicItemDto -> {
            DtoField("Category", item.equipment_category.name)
            DtoField("Rarity", item.rarity.name)
            DtoField("Description", item.desc.joinToString("\n"))
        }
        is com.dnd.helper.data.remote.dto.equipment.WeaponPropertyDto -> {
            DtoField("Description", item.desc.joinToString("\n"))
        }
        is com.dnd.helper.data.remote.dto.monster.MonsterDto -> {
            DtoField("Size & Type", "${item.size} ${item.type}${if (item.subtype != null) " (${item.subtype})" else ""}, ${item.alignment}")
            DtoField("Armor Class", item.armor_class.firstOrNull()?.let { "${it.value} (${it.type})" } ?: "")
            DtoField("Hit Points", "${item.hit_points} (${item.hit_dice})")
            DtoField("Speed", listOfNotNull(
                item.speed.walk?.let { "walk: $it" },
                item.speed.burrow?.let { "burrow: $it" },
                item.speed.climb?.let { "climb: $it" },
                item.speed.fly?.let { "fly: $it" },
                item.speed.swim?.let { "swim: $it" },
                if (item.speed.hover == true) "hover" else null
            ).joinToString())
            DtoField("Stats", "STR: ${item.strength} | DEX: ${item.dexterity} | CON: ${item.constitution} | INT: ${item.intelligence} | WIS: ${item.wisdom} | CHA: ${item.charisma}")
            DtoField("Challenge Rating", "${item.challenge_rating} (${item.xp} XP)")
            if (item.desc.isNotEmpty()) DtoField("Description", item.desc)
        }
        is com.dnd.helper.data.remote.dto.game.ConditionDto -> {
            DtoField("Description", item.desc.joinToString("\n"))
        }
        is com.dnd.helper.data.remote.dto.game.DamageTypeDto -> {
            DtoField("Description", item.desc.joinToString("\n"))
        }
        is com.dnd.helper.data.remote.dto.game.RuleDto -> {
            DtoField("Description", item.desc)
            if (item.subsections.isNotEmpty()) DtoField("Subsections", item.subsections.joinToString { it.name })
        }
        is com.dnd.helper.data.remote.dto.game.RuleSectionDto -> {
            DtoField("Description", item.desc)
        }
    }
}

@Composable
fun DtoField(label: String, value: String) {
    if (value.isBlank()) return
    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
