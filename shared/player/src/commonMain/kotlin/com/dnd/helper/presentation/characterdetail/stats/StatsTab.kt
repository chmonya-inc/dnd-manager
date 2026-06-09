package com.dnd.helper.presentation.characterdetail.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.DndSkill
import com.dnd.helper.domain.model.abilityModifier
import com.dnd.helper.domain.model.modifier

@Composable
fun StatsTab(character: Character) {
    val stats = character.stats
    val proficiencies = character.proficiencies
    val profBonus = character.combat.proficiencyBonus

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Ability Scores Grid
        Text(
            text = "Ability Scores",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val abilities = listOf(
            AbilityData("STR", stats.strength, Icons.Default.FitnessCenter, Color(0xFFE53935)),
            AbilityData("DEX", stats.dexterity, Icons.AutoMirrored.Filled.DirectionsRun, Color(0xFF43A047)),
            AbilityData("CON", stats.constitution, Icons.Default.Shield, Color(0xFFFB8C00)),
            AbilityData("INT", stats.intelligence, Icons.Default.Psychology, Color(0xFF1E88E5)),
            AbilityData("WIS", stats.wisdom, Icons.Default.Visibility, Color(0xFF8E24AA)),
            AbilityData("CHA", stats.charisma, Icons.Default.Star, Color(0xFFFDD835)),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(abilities) { ability ->
                AbilityCard(ability)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Saving Throws
        Text(
            text = "Saving Throws",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        val savingThrows = listOf(
            "strength" to "Strength",
            "dexterity" to "Dexterity",
            "constitution" to "Constitution",
            "intelligence" to "Intelligence",
            "wisdom" to "Wisdom",
            "charisma" to "Charisma",
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                savingThrows.forEach { (key, label) ->
                    val abilityMod = stats.modifier(key)
                    val proficient = proficiencies.savingThrows.any { it.equals(key, ignoreCase = true) }
                    val total = abilityMod + (if (proficient) profBonus else 0)
                    SaveOrSkillRow(
                        label = label,
                        modifier = total,
                        proficient = proficient,
                        profBonus = profBonus
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Skills
        Text(
            text = "Skills",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val allSkills = DndSkill.entries
                allSkills.chunked(2).forEach { rowSkills ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowSkills.forEach { skill ->
                            val abilityMod = stats.modifier(skill.ability)
                            val proficient = proficiencies.skills.any {
                                it.equals(skill.name, ignoreCase = true) ||
                                it.equals(skill.displayName, ignoreCase = true)
                            }
                            val total = abilityMod + (if (proficient) profBonus else 0)
                            
                            Box(modifier = Modifier.weight(1f)) {
                                SaveOrSkillRow(
                                    label = skill.displayName,
                                    modifier = total,
                                    proficient = proficient,
                                    profBonus = profBonus
                                )
                            }
                        }
                        if (rowSkills.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Add spacer to prevent overlap with FAB
        Spacer(modifier = Modifier.height(80.dp))
    }
}

private data class AbilityData(
    val label: String,
    val score: Int,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun AbilityCard(data: AbilityData) {
    val mod = abilityModifier(data.score)
    val modText = if (mod >= 0) "+$mod" else "$mod"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = data.color.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, data.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = data.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = data.color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(data.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = modText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = data.color
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = data.score.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SaveOrSkillRow(
    label: String,
    modifier: Int,
    proficient: Boolean,
    profBonus: Int
) {
    val modText = if (modifier >= 0) "+$modifier" else "$modifier"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Proficiency dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (proficient) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = modText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (proficient) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
