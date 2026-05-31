package com.dnd.helper.presentation.characterdetail.features

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnd.helper.domain.model.Character

@Composable
fun FeaturesTab(character: Character) {
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
        if (profs.armor.isNotEmpty() || profs.weapons.isNotEmpty() || profs.tools.isNotEmpty() || profs.languages.isNotEmpty()) {
            Text(
                text = "Proficiencies",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (profs.armor.isNotEmpty()) {
                ProficiencySection("Armor", profs.armor)
            }
            if (profs.weapons.isNotEmpty()) {
                ProficiencySection("Weapons", profs.weapons)
            }
            if (profs.tools.isNotEmpty()) {
                ProficiencySection("Tools", profs.tools)
            }
            if (profs.languages.isNotEmpty()) {
                ProficiencySection("Languages", profs.languages)
            }
        }

        // Class Features
        if (character.features.classFeatures.isNotEmpty()) {
            FeatureSection(
                title = "Class Features",
                items = character.features.classFeatures
            )
        }

        // Racial Traits
        if (character.features.racialTraits.isNotEmpty()) {
            FeatureSection(
                title = "Racial Traits",
                items = character.features.racialTraits
            )
        }

        // Feats
        if (character.features.feats.isNotEmpty()) {
            FeatureSection(
                title = "Feats",
                items = character.features.feats
            )
        }

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
private fun ProficiencySection(title: String, items: List<String>) {
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
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureSection(title: String, items: List<String>) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            items.forEachIndexed { index, item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
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
