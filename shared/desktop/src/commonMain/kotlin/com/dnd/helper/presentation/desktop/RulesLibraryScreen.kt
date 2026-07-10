package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnd.helper.theme.DndIcons
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
                val hasQuery = state.searchQuery.isNotBlank()
                val matchCount = if (hasQuery) {
                    // Quick count matches in this category
                    countCategoryMatches(state, category, state.searchQuery)
                } else {
                    0
                }

                NavigationRailItem(
                    selected = state.selectedCategory == category,
                    onClick = { viewModel.setCategory(category) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (hasQuery && matchCount > 0) {
                                    Badge { Text(matchCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = when (category) {
                                    RuleCategory.CharacterData -> Icons.Default.Person
                                    RuleCategory.Spells -> DndIcons.Filled.AutoFixHigh
                                    RuleCategory.Equipment -> DndIcons.Filled.Shield
                                    RuleCategory.Monsters -> DndIcons.Filled.Pets
                                    RuleCategory.Mechanics -> DndIcons.Filled.Build
                                    RuleCategory.Rules -> DndIcons.Filled.MenuBook
                                },
                                contentDescription = category.name
                            )
                        }
                    },
                    label = { Text(category.name, maxLines = 1) }
                )
            }
        }

        VerticalDivider()

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search rules, spells, monsters...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                CategoryDataFragment(state, state.selectedCategory)
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

private fun countCategoryMatches(
    state: RulesLibraryState,
    category: RuleCategory,
    query: String
): Int {
    val lists = when (category) {
        RuleCategory.CharacterData -> listOf(
            state.classes,
            state.races,
            state.subraces,
            state.subclasses,
            state.feats,
            state.traits,
            state.features,
            state.backgrounds,
            state.skills,
            state.abilityScores,
            state.alignments,
            state.languages,
            state.proficiencies
        )

        RuleCategory.Spells -> listOf(state.spells, state.magicSchools)
        RuleCategory.Equipment -> listOf(
            state.equipmentCategories,
            state.magicItems,
            state.weaponProperties
        )

        RuleCategory.Monsters -> listOf(state.monsters)
        RuleCategory.Mechanics -> listOf(state.conditions, state.damageTypes)
        RuleCategory.Rules -> listOf(state.rules, state.ruleSections)
    }
    return lists.sumOf { list ->
        list.count { getDtoTitle(it).contains(query, ignoreCase = true) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDataFragment(state: RulesLibraryState, category: RuleCategory) {
    val query = state.searchQuery

    val categoryItems: List<Pair<String, List<Any>>> = remember(state, category, query) {
        val filter: (Any) -> Boolean = { item ->
            if (query.isBlank()) {
                true
            } else {
                getDtoTitle(item).contains(query, ignoreCase = true)
            }
        }

        when (category) {
            RuleCategory.CharacterData -> listOf(
                "Classes" to state.classes.filter(filter),
                "Races" to state.races.filter(filter),
                "Subraces" to state.subraces.filter(filter),
                "Subclasses" to state.subclasses.filter(filter),
                "Feats" to state.feats.filter(filter),
                "Traits" to state.traits.filter(filter),
                "Features" to state.features.filter(filter),
                "Backgrounds" to state.backgrounds.filter(filter),
                "Skills" to state.skills.filter(filter),
                "Ability Scores" to state.abilityScores.filter(filter),
                "Alignments" to state.alignments.filter(filter),
                "Languages" to state.languages.filter(filter),
                "Proficiencies" to state.proficiencies.filter(filter)
            )

            RuleCategory.Spells -> listOf(
                "Spells" to state.spells.filter(filter),
                "Magic Schools" to state.magicSchools.filter(filter)
            )

            RuleCategory.Equipment -> listOf(
                "Equipment Categories" to state.equipmentCategories.filter(filter),
                "Magic Items" to state.magicItems.filter(filter),
                "Weapon Properties" to state.weaponProperties.filter(filter)
            )

            RuleCategory.Monsters -> listOf(
                "Monsters" to state.monsters.filter(filter)
            )

            RuleCategory.Mechanics -> listOf(
                "Conditions" to state.conditions.filter(filter),
                "Damage Types" to state.damageTypes.filter(filter)
            )

            RuleCategory.Rules -> listOf(
                "Rules" to state.rules.filter(filter),
                "Rule Sections" to state.ruleSections.filter(filter)
            )
        }
    }

    var selectedType by androidx.compose.runtime.saveable.rememberSaveable(categoryItems) {
        mutableStateOf(categoryItems.firstOrNull()?.first ?: "")
    }

    val filteredCategoryItems = remember(categoryItems) {
        if (query.isBlank()) {
            categoryItems
        } else {
            categoryItems.filter { it.second.isNotEmpty() }
        }
    }

    // fallback if selection becomes invalid due to data loading or search
    LaunchedEffect(filteredCategoryItems) {
        if (filteredCategoryItems.isNotEmpty() && filteredCategoryItems.none { it.first == selectedType }) {
            selectedType = filteredCategoryItems.first().first
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
            items(filteredCategoryItems) { (type, list) ->
                val isSelected = selectedType == type
                Surface(
                    onClick = { selectedType = type },
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(
                            alpha = 0.5f
                        )
                    },
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
                            containerColor = if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                    alpha = 0.2f
                                )
                            } else {
                                MaterialTheme.colorScheme.secondaryContainer
                            },
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
        val currentList =
            categoryItems.find { it.first == selectedType }?.second ?: emptyList<Any>()

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
                    imageVector = if (expanded) DndIcons.Filled.ExpandLess else DndIcons.Filled.ExpandMore,
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
        is com.dnd.helper.data.remote.dto.character.AbilityScoreDto -> item.fullName
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
            DtoField("Hit Die", "d${item.hitDie}")
            DtoField("Proficiencies", item.proficiencies.joinToString { it.name })
            DtoField("Saving Throws", item.savingThrows.joinToString { it.name })
            DtoField("Subclasses", item.subclasses.joinToString { it.name })
            if (item.startingEquipment.isNotEmpty()) {
                DtoField(
                    "Starting Equipment",
                    item.startingEquipment.joinToString { "${it.quantity}x ${it.equipment.name}" }
                )
            }
            val spellcasting = item.spellcasting
            if (spellcasting != null) {
                DtoField("Spellcasting Ability", spellcasting.spellcastingAbility.name)
            }
        }

        is com.dnd.helper.data.remote.dto.character.RaceDto -> {
            DtoField("Speed", "${item.speed} ft.")
            DtoField("Alignment", item.alignment)
            DtoField("Size", "${item.size} - ${item.sizeDescription}")
            DtoField(
                "Ability Bonuses",
                item.abilityBonuses.joinToString { "${it.abilityScore.name} +${it.bonus}" }
            )
            DtoField("Languages", item.languageDesc)
            DtoField("Traits", item.traits.joinToString { it.name })
            if (item.subraces.isNotEmpty()) {
                DtoField(
                    "Subraces",
                    item.subraces.joinToString { it.name }
                )
            }
        }

        is com.dnd.helper.data.remote.dto.character.SubraceDto -> {
            DtoField("Parent Race", item.race.name)
            DtoField("Description", item.desc)
            DtoField(
                "Ability Bonuses",
                item.abilityBonuses.joinToString { "${it.abilityScore.name} +${it.bonus}" }
            )
            DtoField("Racial Traits", item.racialTraits.joinToString { it.name })
        }

        is com.dnd.helper.data.remote.dto.character.SubclassDto -> {
            DtoField("Parent Class", item.`class`.name)
            DtoField("Flavor", item.subclassFlavor)
            DtoField("Description", item.desc.joinToString("\n"))
        }

        is com.dnd.helper.data.remote.dto.character.FeatDto -> {
            DtoField("Description", item.desc.joinToString("\n"))
            if (item.prerequisites.isNotEmpty()) {
                DtoField(
                    "Prerequisites",
                    item.prerequisites.joinToString { req ->
                        val name = req.abilityScore?.name ?: ""
                        val score = req.minimumScore?.toInt()?.toString() ?: ""
                        if (name.isNotEmpty() && score.isNotEmpty()) "$name $score" else "Other"
                    }
                )
            }
        }

        is com.dnd.helper.data.remote.dto.character.TraitDto -> {
            DtoField("Description", item.desc.joinToString("\n"))
            if (item.races.isNotEmpty()) DtoField("Races", item.races.joinToString { it.name })
            if (item.subraces.isNotEmpty()) {
                DtoField(
                    "Subraces",
                    item.subraces.joinToString { it.name }
                )
            }
        }

        is com.dnd.helper.data.remote.dto.character.FeatureDto -> {
            DtoField("Level", item.level.toString())
            DtoField("Class", item.`class`.name)
            if (item.subclass != null) DtoField("Subclass", item.subclass!!.name)
            DtoField("Description", item.desc.joinToString("\n"))
            if (item.prerequisites.isNotEmpty()) {
                DtoField(
                    "Prerequisites",
                    item.prerequisites.joinToString { req ->
                        req.feature ?: req.spell ?: "Level ${req.level ?: "?"}"
                    }
                )
            }
        }

        is com.dnd.helper.data.remote.dto.character.BackgroundDto -> {
            DtoField("Starting Proficiencies", item.startingProficiencies.joinToString { it.name })
            DtoField("Feature", item.feature.name)
            if (item.feature.desc.isNotEmpty()) {
                DtoField("Feature Description", item.feature.desc.joinToString("\n"))
            }
            if (item.startingEquipment.isNotEmpty()) {
                DtoField(
                    "Starting Equipment",
                    item.startingEquipment.joinToString { "${it.quantity}x ${it.equipment.name}" }
                )
            }
        }

        is com.dnd.helper.data.remote.dto.character.DndSkillDto -> {
            DtoField("Ability Score", item.abilityScore.name)
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
            if (item.typicalSpeakers.isNotEmpty()) {
                DtoField(
                    "Typical Speakers",
                    item.typicalSpeakers.joinToString()
                )
            }
        }

        is com.dnd.helper.data.remote.dto.character.ProficiencyDto -> {
            DtoField("Type", item.type)
            if (item.classes.isNotEmpty()) {
                DtoField(
                    "Classes",
                    item.classes.joinToString { it.name }
                )
            }
            if (item.races.isNotEmpty()) DtoField("Races", item.races.joinToString { it.name })
        }

        is com.dnd.helper.data.remote.dto.spell.SpellDto -> {
            DtoField("Level", if (item.level == 0) "Cantrip" else item.level.toString())
            DtoField("School", item.school.name)
            DtoField("Casting Time", item.castingTime)
            DtoField("Range", item.range)
            DtoField(
                "Components",
                item.components.joinToString() + if (item.material != null) " (${item.material})" else ""
            )
            DtoField("Duration", item.duration + if (item.concentration) " (Concentration)" else "")
            DtoField("Classes", item.classes.joinToString { it.name })
            DtoField("Description", item.desc.joinToString("\n"))
            if (item.higherLevel.isNotEmpty()) {
                DtoField(
                    "At Higher Levels",
                    item.higherLevel.joinToString("\n")
                )
            }
        }

        is com.dnd.helper.data.remote.dto.spell.MagicSchoolDto -> {
            DtoField("Description", item.desc)
        }

        is com.dnd.helper.data.remote.dto.equipment.EquipmentCategoryDto -> {
            DtoField("Equipment Count", item.equipment.size.toString())
            DtoField(
                "Items",
                item.equipment.take(10)
                    .joinToString { it.name } + if (item.equipment.size > 10) "..." else ""
            )
        }

        is com.dnd.helper.data.remote.dto.equipment.MagicItemDto -> {
            DtoField("Category", item.equipmentCategory.name)
            DtoField("Rarity", item.rarity.name)
            DtoField("Description", item.desc.joinToString("\n"))
        }

        is com.dnd.helper.data.remote.dto.equipment.WeaponPropertyDto -> {
            DtoField("Description", item.desc.joinToString("\n"))
        }

        is com.dnd.helper.data.remote.dto.monster.MonsterDto -> {
            DtoField(
                "Size & Type",
                "${item.size} ${item.type}${if (item.subtype != null) " (${item.subtype})" else ""}, ${item.alignment}"
            )
            DtoField(
                "Armor Class",
                item.armorClass.firstOrNull()?.let { "${it.value} (${it.type})" } ?: ""
            )
            DtoField("Hit Points", "${item.hitPoints} (${item.hitDice})")
            DtoField(
                "Speed",
                listOfNotNull(
                    item.speed.walk?.let { "walk: $it" },
                    item.speed.burrow?.let { "burrow: $it" },
                    item.speed.climb?.let { "climb: $it" },
                    item.speed.fly?.let { "fly: $it" },
                    item.speed.swim?.let { "swim: $it" },
                    if (item.speed.hover == true) "hover" else null
                ).joinToString()
            )
            DtoField(
                "Stats",
                "STR: ${item.strength} | DEX: ${item.dexterity} | CON: ${item.constitution} | INT: ${item.intelligence} | WIS: ${item.wisdom} | CHA: ${item.charisma}"
            )
            DtoField("Challenge Rating", "${item.challengeRating} (${item.xp} XP)")
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
            if (item.subsections.isNotEmpty()) {
                DtoField(
                    "Subsections",
                    item.subsections.joinToString { it.name }
                )
            }
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
