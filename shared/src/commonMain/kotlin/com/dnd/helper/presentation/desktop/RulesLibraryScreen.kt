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
            when (state.selectedCategory) {
                RuleCategory.CharacterData -> CharacterDataFragment(state)
                else -> {
                    // Placeholders for other types
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "${state.selectedCategory.name} - Not Implemented Yet",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDataFragment(state: RulesLibraryState) {
    var selectedType by remember { mutableStateOf("Classes") }

    val actualTypes = listOf(
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

    Row(modifier = Modifier.fillMaxSize()) {
        // Secondary sidebar
        LazyColumn(
            modifier = Modifier.width(200.dp).fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(actualTypes) { (type, list) ->
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
                        Text(type, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        Badge { Text(list.size.toString()) }
                    }
                }
            }
        }

        VerticalDivider()

        // List of items
        val currentList = actualTypes.find { it.first == selectedType }?.second ?: emptyList<Any>()
        
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
                    fontWeight = FontWeight.Bold
                )
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
            if (item.spellcasting != null) {
                DtoField("Spellcasting Ability", item.spellcasting.spellcasting_ability.name)
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
            if (item.subclass != null) DtoField("Subclass", item.subclass.name)
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
            if (item.script != null) DtoField("Script", item.script)
            if (item.desc != null) DtoField("Description", item.desc)
            if (item.typical_speakers.isNotEmpty()) DtoField("Typical Speakers", item.typical_speakers.joinToString())
        }
        is com.dnd.helper.data.remote.dto.character.ProficiencyDto -> {
            DtoField("Type", item.type)
            if (item.classes.isNotEmpty()) DtoField("Classes", item.classes.joinToString { it.name })
            if (item.races.isNotEmpty()) DtoField("Races", item.races.joinToString { it.name })
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
