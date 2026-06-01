package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.Skill
import com.dnd.helper.domain.model.abilityModifier
import com.dnd.helper.presentation.characterlist.CharacterListEvent
import com.dnd.helper.presentation.characterlist.CharacterListScreen
import com.dnd.helper.presentation.characterlist.CharacterListViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MasterSummaryScreen(
    onCharacterClick: (String) -> Unit,
    viewModel: CharacterListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(viewModel) {
        viewModel.startAutoRefresh()
        onDispose { viewModel.stopAutoRefresh() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Party Summary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "${state.characters.size} characters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(16.dp))

        if (state.isLoading && state.characters.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 380.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(state.characters, key = { it.id }) { character ->
                    SummaryCharacterCard(
                        character = character,
                        onClick = { onCharacterClick(character.id) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCharacterCard(
    character: Character,
    onClick: () -> Unit,
) {
    val hpRatio = if (character.maxHp > 0) {
        (character.currentHp.toFloat() / character.maxHp).coerceIn(0f, 1f)
    } else 0f
    val hpColor = when {
        character.currentHp <= 0 -> Color(0xFF9E9E9E)
        hpRatio <= 0.4f -> Color(0xFFD32F2F)
        else -> Color(0xFF43A047)
    }
    val isDying = character.currentHp <= 0 && !character.combat.conditions.contains("Dead")
    val isDead = character.currentHp <= 0 && character.combat.deathSaveFailures >= 3

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row: Avatar + Name + Level + AC
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!character.displayImageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = character.displayImageUrl,
                            contentDescription = character.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }

                // Name & Class
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${character.race} ${character.characterClass}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Level badge
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "LVL ${character.level}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // AC badge
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                        Text(
                            text = "${character.combat.armorClass}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // HP Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = if (character.currentHp <= 0) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = hpColor,
                )
                LinearProgressIndicator(
                    progress = { hpRatio },
                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = hpColor,
                    trackColor = hpColor.copy(alpha = 0.2f),
                )
                Text(
                    text = "${character.currentHp}/${character.maxHp}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = hpColor,
                )
            }

            // Death saves if dying
            if (isDying) {
                Spacer(Modifier.height(8.dp))
                DeathSavesRow(
                    successes = character.combat.deathSaveSuccesses,
                    failures = character.combat.deathSaveFailures,
                )
            }

            // Dead indicator
            if (isDead) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "✗ Dead",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F),
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(Modifier.height(12.dp))

            // Ability modifiers row
            AbilityModifiersRow(stats = character.stats)

            // Skills
            if (character.skills.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    character.skills.take(8).forEach { skill ->
                        SkillChip(skill = skill)
                    }
                    if (character.skills.size > 8) {
                        Text(
                            text = "+${character.skills.size - 8} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AbilityModifiersRow(stats: com.dnd.helper.domain.model.CharacterStats) {
    val abilities = listOf(
        "STR" to abilityModifier(stats.strength),
        "DEX" to abilityModifier(stats.dexterity),
        "CON" to abilityModifier(stats.constitution),
        "INT" to abilityModifier(stats.intelligence),
        "WIS" to abilityModifier(stats.wisdom),
        "CHA" to abilityModifier(stats.charisma),
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        abilities.forEach { (label, mod) ->
            val color = when {
                mod > 0 -> Color(0xFF43A047)
                mod < 0 -> Color(0xFFD32F2F)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = if (mod >= 0) "+$mod" else "$mod",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                )
            }
        }
    }
}

@Composable
private fun SkillChip(skill: Skill) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp),
    ) {
        Text(
            text = skill.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Composable
private fun DeathSavesRow(successes: Int, failures: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { i ->
                DeathSaveDot(filled = i < successes, color = Color(0xFF43A047))
            }
        }
        Text(
            text = "Death Saves",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { i ->
                DeathSaveDot(filled = i < failures, color = Color(0xFFD32F2F))
            }
        }
    }
}

@Composable
private fun DeathSaveDot(filled: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(
                if (filled) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            ),
    )
}
