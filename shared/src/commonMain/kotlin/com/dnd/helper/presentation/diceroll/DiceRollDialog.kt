package com.dnd.helper.presentation.diceroll

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.random.Random

enum class DiceType(val sides: Int, val displayName: String) {
    D4(4, "d4"),
    D6(6, "d6"),
    D8(8, "d8"),
    D10(10, "d10"),
    D12(12, "d12"),
    D20(20, "d20"),
    D100(100, "d%"),
}

data class DiceResult(
    val diceType: DiceType,
    val rolls: List<Int>,
    val total: Int,
)

@Composable
fun DiceRollDialog(
    onDismiss: () -> Unit,
) {
    var diceCounts by remember { mutableStateOf(DiceType.entries.associateWith { 0 }) }
    var results by remember { mutableStateOf<List<DiceResult>>(emptyList()) }
    var rollingDice by remember { mutableStateOf<Set<DiceType>>(emptySet()) }
    val scope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Dice Roller",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Dice icons row — tap to roll single die
                Text(
                    text = "Tap a die to roll one",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    DiceType.entries.forEach { diceType ->
                        SingleDiceButton(
                            diceType = diceType,
                            isRolling = diceType in rollingDice,
                            onRoll = {
                                scope.launch {
                                    rollingDice = rollingDice + diceType
                                    delay(450)
                                    val roll = Random.nextInt(1, diceType.sides + 1)
                                    results = listOf(
                                        DiceResult(diceType, listOf(roll), roll)
                                    ) + results
                                    rollingDice = rollingDice - diceType
                                }
                            },
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Batch counts
                Text(
                    text = "Set counts for batch roll",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))

                // First 4 dice counts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    DiceType.entries.take(2).forEach { diceType ->
                        DiceCountSelector(
                            diceType = diceType,
                            count = diceCounts[diceType] ?: 0,
                            onChange = { newCount ->
                                diceCounts = diceCounts.toMutableMap().apply {
                                    put(diceType, newCount.coerceAtLeast(0))
                                }
                            },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    DiceType.entries.drop(2).take(2).forEach { diceType ->
                        DiceCountSelector(
                            diceType = diceType,
                            count = diceCounts[diceType] ?: 0,
                            onChange = { newCount ->
                                diceCounts = diceCounts.toMutableMap().apply {
                                    put(diceType, newCount.coerceAtLeast(0))
                                }
                            },
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    DiceType.entries.drop(4).take(2).forEach { diceType ->
                        DiceCountSelector(
                            diceType = diceType,
                            count = diceCounts[diceType] ?: 0,
                            onChange = { newCount ->
                                diceCounts = diceCounts.toMutableMap().apply {
                                    put(diceType, newCount.coerceAtLeast(0))
                                }
                            },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                // Last 3 dice counts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    DiceType.entries.drop(6).forEach { diceType ->
                        DiceCountSelector(
                            diceType = diceType,
                            count = diceCounts[diceType] ?: 0,
                            onChange = { newCount ->
                                diceCounts = diceCounts.toMutableMap().apply {
                                    put(diceType, newCount.coerceAtLeast(0))
                                }
                            },
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Roll selected button
                Button(
                    onClick = {
                        val newResults = mutableListOf<DiceResult>()
                        diceCounts.forEach { (diceType, count) ->
                            if (count > 0) {
                                val rolls = List(count) {
                                    Random.nextInt(1, diceType.sides + 1)
                                }
                                newResults.add(
                                    DiceResult(diceType, rolls, rolls.sum())
                                )
                            }
                        }
                        if (newResults.isNotEmpty()) {
                            results = newResults + results
                        }
                    },
                    enabled = diceCounts.values.any { it > 0 },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Roll Selected Dice")
                }

                // Results
                if (results.isNotEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "Results",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.align(Alignment.Start),
                    )
                    Spacer(Modifier.height(6.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 180.dp),
                    ) {
                        items(results) { result ->
                            ResultRow(result)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingleDiceButton(
    diceType: DiceType,
    isRolling: Boolean,
    onRoll: () -> Unit,
) {
    val targetRotation = if (isRolling) 360f else 0f
    val rotation by animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = tween(durationMillis = 450),
        label = "dice_rotation",
    )

    val bgColor = when (diceType) {
        DiceType.D4 -> MaterialTheme.colorScheme.primaryContainer
        DiceType.D6 -> MaterialTheme.colorScheme.secondaryContainer
        DiceType.D8 -> MaterialTheme.colorScheme.tertiaryContainer
        DiceType.D10 -> MaterialTheme.colorScheme.primaryContainer
        DiceType.D12 -> MaterialTheme.colorScheme.secondaryContainer
        DiceType.D20 -> MaterialTheme.colorScheme.tertiaryContainer
        DiceType.D100 -> MaterialTheme.colorScheme.primaryContainer
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .rotate(rotation)
            .background(
                color = if (isRolling) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                } else {
                    bgColor
                },
                shape = RoundedCornerShape(10.dp),
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(5.dp)
            .clickable(
                enabled = !isRolling,
                onClick = onRoll,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = diceType.displayName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DiceCountSelector(
    diceType: DiceType,
    count: Int,
    onChange: (Int) -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = diceType.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                    )
                    .clickable { onChange(count - 1) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp),
                textAlign = TextAlign.Center,
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp),
                    )
                    .clickable { onChange(count + 1) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase",
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ResultRow(result: DiceResult) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = result.diceType.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(36.dp),
            )
            Text(
                text = result.rolls.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        if (result.rolls.size > 1) {
            Text(
                text = "= ${result.total}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
