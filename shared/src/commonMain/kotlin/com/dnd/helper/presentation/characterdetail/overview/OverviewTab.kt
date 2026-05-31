package com.dnd.helper.presentation.characterdetail.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.model.abilityModifier
import com.dnd.helper.domain.model.modifier

@Composable
fun OverviewTab(character: Character) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Card
        HeaderCard(character)

        // Appearance
        if (character.appearance.age > 0 || character.appearance.gender.isNotBlank()) {
            AppearanceRow(character)
        }

        // Combat Summary
        CombatSummaryCard(character)

        // Status Bar
        StatusBar(character)

        // Inspiration & Exhaustion
        InspirationExhaustionRow(character)

        // Conditions
        if (character.combat.conditions.isNotEmpty()) {
            ConditionsChipRow(character.combat.conditions)
        }
    }
}

@Composable
private fun HeaderCard(character: Character) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = character.imageUrl,
                contentDescription = character.name,
                modifier = Modifier.size(200.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Fit,
                onError = { state ->
                    println("[AsyncImage] Failed to load avatar for ${character.name}: ${state.result.throwable}")
                },
            )
            Text(
                text = character.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = character.race,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = character.characterClass,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                if (character.subclass.isNotBlank()) {
                    Text(
                        text = "(${character.subclass})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LevelBadge(character.level)
                if (character.background.isNotBlank()) {
                    Text(
                        text = character.background,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (character.experiencePoints > 0) {
                    Text(
                        text = "${character.experiencePoints} XP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelBadge(level: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "LVL $level",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceRow(character: Character) {
    val appearance = character.appearance
    val chips = buildList {
        if (appearance.age > 0) add("${appearance.age} yrs")
        if (appearance.gender.isNotBlank()) add(appearance.gender)
        if (appearance.height.isNotBlank()) add(appearance.height)
        if (appearance.weight.isNotBlank()) add(appearance.weight)
        if (appearance.eyes.isNotBlank()) add("Eyes: ${appearance.eyes}")
        if (appearance.hair.isNotBlank()) add("Hair: ${appearance.hair}")
        if (appearance.skin.isNotBlank()) add("Skin: ${appearance.skin}")
    }

    if (chips.isNotEmpty()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            chips.forEach { chip ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = chip,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CombatSummaryCard(character: Character) {
    val combat = character.combat
    val totalHp = character.currentHp + combat.tempHp
    val hpRatio = (character.currentHp.toFloat() / character.maxHp).coerceIn(0f, 1f)
    val hpColor = when {
        character.currentHp <= 0 -> Color(0xFF9E9E9E)
        hpRatio <= 0.4f -> Color(0xFFD32F2F)
        else -> Color(0xFFE53935)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                // AC
                CombatStat(
                    icon = Icons.Default.Shield,
                    value = combat.armorClass.toString(),
                    label = "AC",
                    tint = MaterialTheme.colorScheme.primary
                )

                // HP
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (character.currentHp <= 0) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = hpColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalHp / ${character.maxHp}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = hpColor
                        )
                    }
                    if (combat.tempHp > 0) {
                        Text(
                            text = "+${combat.tempHp} temp",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1E88E5)
                        )
                    }
                    Text(
                        text = "Hit Points",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LinearProgressIndicator(
                        progress = { hpRatio },
                        modifier = Modifier
                            .width(100.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = hpColor,
                        trackColor = hpColor.copy(alpha = 0.2f)
                    )
                }

                // Hit Dice
                CombatStat(
                    value = "${combat.hitDiceCurrent}/${character.level}",
                    label = combat.hitDice,
                    tint = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Death Saves
            if (character.currentHp <= 0) {
                DeathSaves(combat.deathSaveSuccesses, combat.deathSaveFailures)
            }
        }
    }
}

@Composable
private fun CombatStat(
    value: String,
    label: String,
    tint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeathSaves(successes: Int, failures: Int) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Death Saving Throws",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Successes:", style = MaterialTheme.typography.labelSmall)
                repeat(3) { i ->
                    DeathSaveDiamond(filled = i < successes, color = Color(0xFF43A047))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Failures:", style = MaterialTheme.typography.labelSmall)
                repeat(3) { i ->
                    DeathSaveDiamond(filled = i < failures, color = Color(0xFFD32F2F))
                }
            }
        }
    }
}

@Composable
private fun DeathSaveDiamond(filled: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(if (filled) color else color.copy(alpha = 0.15f))
    )
}

@Composable
private fun StatusBar(character: Character) {
    val combat = character.combat
    val stats = character.stats
    val profBonus = combat.proficiencyBonus
    val wisMod = abilityModifier(stats.wisdom)
    val perceptionProficient = character.proficiencies.skills.any { it.equals("perception", ignoreCase = true) }
    val passivePerception = 10 + wisMod + (if (perceptionProficient) profBonus else 0)

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusItem("Initiative", "${if (combat.initiative >= 0) "+" else ""}${combat.initiative}")
            StatusItem("Speed", "${combat.speed} ft")
            StatusItem("Perception", passivePerception.toString())
            StatusItem("Proficiency", "+$profBonus")
        }
    }
}

@Composable
private fun StatusItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InspirationExhaustionRow(character: Character) {
    val combat = character.combat

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Inspiration
        Card(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Inspiration", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            if (combat.inspiration) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (combat.inspiration) {
                        Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Exhaustion
        Card(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Exhaustion", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { level ->
                        val active = level == combat.exhaustion
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        active -> Color(0xFFFB8C00)
                                        level == 6 -> Color(0xFFD32F2F).copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.toString(),
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConditionsChipRow(conditions: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        conditions.forEach { condition ->
            Surface(
                color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f))
            ) {
                Text(
                    text = condition,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFF8A80)
                )
            }
        }
    }
}
