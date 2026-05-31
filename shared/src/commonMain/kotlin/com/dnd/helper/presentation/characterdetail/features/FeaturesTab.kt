package com.dnd.helper.presentation.characterdetail.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnd.helper.domain.model.Character

@Composable
fun FeaturesTab(
    character: Character,
    isMasterMode: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Background & Subclass
        if (character.background.isNotBlank() || character.subclass.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (character.background.isNotBlank()) {
                        Text(
                            text = "Background",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = character.background,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (character.subclass.isNotBlank()) {
                        if (character.background.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = "Subclass",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = character.subclass,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Proficiencies
        val profs = character.proficiencies
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Proficiencies",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (isMasterMode) {
                IconButton(onClick = { /* TODO: onEvent(CharacterDetailEvent.ShowAddProficiencyDialog) */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Proficiency")
                }
            }
        }

        if (profs.armor.isNotEmpty()) {
            ProficiencySection("Armor", profs.armor, isMasterMode)
        }
        if (profs.weapons.isNotEmpty()) {
            ProficiencySection("Weapons", profs.weapons, isMasterMode)
        }
        if (profs.tools.isNotEmpty()) {
            ProficiencySection("Tools", profs.tools, isMasterMode)
        }
        if (profs.languages.isNotEmpty()) {
            ProficiencySection("Languages", profs.languages, isMasterMode)
        }

        // Class Features
        FeatureSection(
            title = "Class Features",
            items = character.features.classFeatures,
            isMasterMode = isMasterMode
        )

        // Racial Traits
        FeatureSection(
            title = "Racial Traits",
            items = character.features.racialTraits,
            isMasterMode = isMasterMode
        )

        // Feats
        FeatureSection(
            title = "Feats",
            items = character.features.feats,
            isMasterMode = isMasterMode
        )

        // Biography
        if (character.description.isNotBlank()) {
            Text(
                text = "Biography",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = character.description,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProficiencySection(
    title: String,
    items: List<String>,
    isMasterMode: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        onClick = { if (isMasterMode) { /* TODO: Delete proficiency */ } },
                        enabled = isMasterMode
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            if (isMasterMode) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureSection(
    title: String,
    items: List<String>,
    isMasterMode: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (isMasterMode) {
            IconButton(onClick = { /* TODO: onEvent(CharacterDetailEvent.ShowAddFeatureDialog(title)) */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add $title")
            }
        }
    }

    if (items.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No items added",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                items.forEachIndexed { index, item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "• $item",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp).weight(1f)
                        )
                        if (isMasterMode) {
                            IconButton(
                                onClick = { /* TODO: Delete feature */ },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}
