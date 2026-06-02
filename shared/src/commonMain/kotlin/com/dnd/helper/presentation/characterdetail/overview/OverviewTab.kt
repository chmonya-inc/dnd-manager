package com.dnd.helper.presentation.characterdetail.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.model.abilityModifier
import com.dnd.helper.domain.model.modifier
import com.dnd.helper.presentation.characterdetail.CharacterDetailEvent

@Composable
fun OverviewTab(
    character: Character,
    onEvent: (CharacterDetailEvent) -> Unit,
    lastDeathSaveRoll: Int? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header Card
        HeaderCard(character, onEvent)

        // Appearance
        if (character.appearance.age > 0 || character.appearance.gender.isNotBlank()) {
            AppearanceRow(character)
        }

        // Combat Summary
        CombatSummaryCard(character, onEvent, lastDeathSaveRoll)

        // Stat Modifiers Row
        StatModifiersRow(character.stats)

        // Status Bar
        StatusBar(character)

        // Inspiration & Exhaustion
        InspirationExhaustionRow(character, onEvent)

        // Ability Scores (foldable)
        StatControls(character.stats, onEvent)

        // Conditions
        if (character.combat.conditions.isNotEmpty()) {
            ConditionsChipRow(character.combat.conditions)
        }

        // Add spacer to prevent overlap with FAB
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun HeaderCard(
    character: Character,
    onEvent: (CharacterDetailEvent) -> Unit,
) {
    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Character Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                val imageUrl = character.displayImageUrl
                val isGenerating = imageUrl?.startsWith("generating:") == true

                if (!imageUrl.isNullOrBlank()) {
                    if (isGenerating) {
                        CircularProgressIndicator()
                    } else {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = character.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onError = { state ->
                                println("[AsyncImage] Failed to load avatar for ${character.name}: ${state.result.throwable}")
                            },
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = character.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = character.race,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = character.characterClass,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (character.subclass.isNotBlank()) {
                    Text(
                        text = "(${character.subclass})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Level control
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = { onEvent(CharacterDetailEvent.UpdateLevel(-amount)) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp))
                }

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(28.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            MaterialTheme.shapes.small,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicTextField(
                        value = amountText,
                        onValueChange = { if (it.all { it.isDigit() } && it.length < 3) amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }

                IconButton(
                    onClick = { onEvent(CharacterDetailEvent.UpdateLevel(amount)) },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                }

                Spacer(Modifier.width(8.dp))

                LevelBadge(character.level)

                if (character.background.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = character.background,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (character.experiencePoints > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${character.experiencePoints} XP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = "LVL $level",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chips.forEach { chip ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = chip,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun CombatSummaryCard(
    character: Character,
    onEvent: (CharacterDetailEvent) -> Unit,
    lastDeathSaveRoll: Int? = null,
) {
    val combat = character.combat
    val totalHp = character.currentHp + combat.tempHp
    val hpRatio = (character.currentHp.toFloat() / character.maxHp).coerceIn(0f, 1f)
    val hpColor = when {
        character.currentHp <= 0 -> Color(0xFF9E9E9E)
        hpRatio <= 0.4f -> Color(0xFFD32F2F)
        else -> Color(0xFFE53935)
    }

    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // AC / HP / Hit Dice row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top,
            ) {
                CombatStat(
                    icon = Icons.Default.Shield,
                    value = combat.armorClass.toString(),
                    label = "AC",
                    tint = MaterialTheme.colorScheme.primary,
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (character.currentHp <= 0) Icons.Default.FavoriteBorder else Icons.Default.Favorite,
                            contentDescription = null,
                            tint = hpColor,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalHp / ${character.maxHp}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = hpColor,
                        )
                    }
                    if (combat.tempHp > 0) {
                        Text(
                            text = "+${combat.tempHp} temp",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1E88E5),
                        )
                    }
                    Text(
                        text = "Hit Points",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LinearProgressIndicator(
                        progress = { hpRatio },
                        modifier = Modifier
                            .width(100.dp)
                            .height(6.dp)
                            .clip(MaterialTheme.shapes.extraSmall),
                        color = hpColor,
                        trackColor = hpColor.copy(alpha = 0.2f),
                    )
                }

                CombatStat(
                    value = "${combat.hitDiceCurrent}/${character.level}",
                    label = combat.hitDice,
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HP Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Current HP controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedIconButton(
                        onClick = { onEvent(CharacterDetailEvent.UpdateHp(-amount)) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "HP",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedIconButton(
                        onClick = { onEvent(CharacterDetailEvent.UpdateHp(amount)) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    }
                }

                // Amount
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(32.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.shapes.small,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicTextField(
                        value = amountText,
                        onValueChange = { if (it.all { it.isDigit() } && it.length < 4) amountText = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,

                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }

                // Max HP controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedIconButton(
                        onClick = { onEvent(CharacterDetailEvent.UpdateMaxHp(-amount)) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Max",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    OutlinedIconButton(
                        onClick = { onEvent(CharacterDetailEvent.UpdateMaxHp(amount)) },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Death Saves
            if (character.currentHp <= 0) {
                DeathSaves(combat.deathSaveSuccesses, combat.deathSaveFailures, onEvent, lastDeathSaveRoll)
            }
        }
    }
}

@Composable
private fun CombatStat(
    value: String,
    label: String,
    tint: Color,
    icon: ImageVector? = null,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = tint,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DeathSaves(
    successes: Int,
    failures: Int,
    onEvent: (CharacterDetailEvent) -> Unit,
    lastRoll: Int? = null,
) {
    val isStable = successes >= 3
    val isDead = failures >= 3
    val canRoll = !isStable && !isDead

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Death Saving Throws",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // Successes
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Successes", style = MaterialTheme.typography.labelSmall, color = Color(0xFF43A047))
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { i ->
                        DeathSaveDiamond(filled = i < successes, color = Color(0xFF43A047))
                    }
                }
            }
            // Failures
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Failures", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD32F2F))
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) { i ->
                        DeathSaveDiamond(filled = i < failures, color = Color(0xFFD32F2F))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isStable -> Text(
                text = "✓ Stabilized",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF43A047),
            )
            isDead -> Text(
                text = "✗ Dead",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
            )
            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = { onEvent(CharacterDetailEvent.RollDeathSave) }) {
                        Text("Roll Death Save (d20)")
                    }
                    if (lastRoll != null) {
                        val rollColor = if (lastRoll >= 10) Color(0xFF43A047) else Color(0xFFD32F2F)
                        Text(
                            text = "Last roll: $lastRoll",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = rollColor,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeathSaveDiamond(filled: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(
                if (filled) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (filled) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color.White,
            )
        }
    }
}

@Composable
private fun StatModifiersRow(stats: CharacterStats) {
    val statData = listOf(
        StatModifierData("STR", stats.strength, Icons.Default.FitnessCenter, Color(0xFFEF5350)),
        StatModifierData("DEX", stats.dexterity, Icons.AutoMirrored.Filled.DirectionsRun, Color(0xFF66BB6A)),
        StatModifierData("CON", stats.constitution, Icons.Default.HealthAndSafety, Color(0xFFFFA726)),
        StatModifierData("INT", stats.intelligence, Icons.Default.Lightbulb, Color(0xFF42A5F5)),
        StatModifierData("WIS", stats.wisdom, Icons.Default.Visibility, Color(0xFFAB47BC)),
        StatModifierData("CHA", stats.charisma, Icons.Default.Mood, Color(0xFFEC407A)),
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            statData.forEach { stat ->
                val mod = abilityModifier(stat.value)
                val modText = if (mod >= 0) "+$mod" else "$mod"
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = stat.icon,
                        contentDescription = stat.label,
                        modifier = Modifier.size(20.dp),
                        tint = stat.color,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stat.value.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = modText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = stat.color,
                    )
                }
            }
        }
    }
}

private data class StatModifierData(
    val label: String,
    val value: Int,
    val icon: ImageVector,
    val color: Color,
)

@Composable
private fun StatControls(
    stats: CharacterStats,
    onEvent: (CharacterDetailEvent) -> Unit,
) {
    val statConfigs = listOf(
        StatControlConfig("strength", stats.strength, "STR", Icons.Default.FitnessCenter, Color(0xFFEF5350)),
        StatControlConfig("dexterity", stats.dexterity, "DEX", Icons.AutoMirrored.Filled.DirectionsRun, Color(0xFF66BB6A)),
        StatControlConfig("constitution", stats.constitution, "CON", Icons.Default.HealthAndSafety, Color(0xFFFFA726)),
        StatControlConfig("intelligence", stats.intelligence, "INT", Icons.Default.Lightbulb, Color(0xFF42A5F5)),
        StatControlConfig("wisdom", stats.wisdom, "WIS", Icons.Default.Visibility, Color(0xFFAB47BC)),
        StatControlConfig("charisma", stats.charisma, "CHA", Icons.Default.Mood, Color(0xFFEC407A)),
    )

    var amountText by remember { mutableStateOf("1") }
    val amount = amountText.toIntOrNull() ?: 1
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Ability Scores",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(Modifier.height(8.dp))

                    statConfigs.forEach { config ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.width(80.dp),
                    ) {
                        Icon(
                            imageVector = config.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = config.color,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = config.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = config.color,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        IconButton(
                            onClick = { onEvent(CharacterDetailEvent.UpdateStat(config.statName, -amount)) },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(Icons.Default.Remove, null, modifier = Modifier.size(22.dp), tint = config.color)
                        }

                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(32.dp)
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    MaterialTheme.shapes.small,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            BasicTextField(
                                value = amountText,
                                onValueChange = { if (it.all { it.isDigit() } && it.length < 3) amountText = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            )
                        }

                        IconButton(
                            onClick = { onEvent(CharacterDetailEvent.UpdateStat(config.statName, amount)) },
                            modifier = Modifier.size(40.dp),
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(22.dp), tint = config.color)
                        }
                    }

                    Text(
                        text = config.value.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
                }
            }
        }
    }
}

private data class StatControlConfig(
    val statName: String,
    val value: Int,
    val label: String,
    val icon: ImageVector,
    val color: Color,
)

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
            horizontalArrangement = Arrangement.SpaceEvenly,
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
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InspirationExhaustionRow(
    character: Character,
    onEvent: (CharacterDetailEvent) -> Unit,
) {
    val combat = character.combat

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            modifier = Modifier.weight(1f),
            onClick = { onEvent(CharacterDetailEvent.ToggleInspiration) },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text("Inspiration", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (combat.inspiration) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (combat.inspiration) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimary,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        )
                    }
                }
            }
        }

        Card(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Exhaustion", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(7) { level ->
                        val active = level == combat.exhaustion
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(
                                    when {
                                        active -> Color(0xFFFB8C00)
                                        level == 6 -> Color(0xFFD32F2F).copy(alpha = 0.3f)
                                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = level.toString(),
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                color = if (active) Color.Black else MaterialTheme.colorScheme.onSurface,
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        conditions.forEach { condition ->
            Surface(
                color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                shape = MaterialTheme.shapes.small,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f)),
            ) {
                Text(
                    text = condition,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFF8A80),
                )
            }
        }
    }
}
