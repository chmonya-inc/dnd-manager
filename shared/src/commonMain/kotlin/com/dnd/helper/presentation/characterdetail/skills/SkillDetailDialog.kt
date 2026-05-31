package com.dnd.helper.presentation.characterdetail.skills

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.dnd.helper.domain.model.Skill

@Composable
fun SkillDetailDialog(
    skill: Skill,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SkillIconBox(
                            iconUrl = skill.iconUrl,
                            tint = damageTypeColor(skill.damageType),
                            bgTint = damageTypeColor(skill.damageType).copy(alpha = 0.15f),
                            size = 44.dp,
                            iconSize = 28.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = skill.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            if (skill.school.isNotBlank()) {
                                Text(
                                    text = skill.school,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Tags row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (skill.level > 0) {
                        SkillTag("Level ${skill.level}", MaterialTheme.colorScheme.primaryContainer)
                    } else if (skill.isPassive) {
                        SkillTag("Passive", MaterialTheme.colorScheme.tertiaryContainer)
                    } else {
                        SkillTag("Cantrip", MaterialTheme.colorScheme.secondaryContainer)
                    }
                    if (skill.damageType.isNotBlank()) {
                        SkillTag(skill.damageType, damageTypeColor(skill.damageType).copy(alpha = 0.15f))
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Detail fields
                if (skill.resourceCost.isNotBlank()) {
                    DetailRow(label = "Cost", value = skill.resourceCost)
                }
                if (skill.castingTime.isNotBlank()) {
                    DetailRow(label = "Casting", value = skill.castingTime)
                }
                if (skill.range.isNotBlank()) {
                    DetailRow(label = "Range", value = skill.range)
                }
                if (skill.duration.isNotBlank()) {
                    DetailRow(label = "Duration", value = skill.duration)
                }
                if (skill.damage.isNotBlank()) {
                    DetailRow(
                        label = "Damage",
                        value = skill.damage,
                        valueColor = damageTypeColor(skill.damageType),
                    )
                }

                if (skill.description.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = skill.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillTag(text: String, bgColor: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}

@Composable
fun damageTypeColor(type: String): Color = when (type.lowercase()) {
    "fire" -> Color(0xFFFF5722)
    "cold", "ice", "frost" -> Color(0xFF03A9F4)
    "lightning", "thunder", "shock" -> Color(0xFFFFC107)
    "necrotic", "death", "void" -> Color(0xFF9C27B0)
    "poison", "toxic", "acid", "venom" -> Color(0xFF4CAF50)
    "psychic", "mind", "charm" -> Color(0xFFE91E63)
    "radiant", "holy", "sun", "divine" -> Color(0xFFFFA000)
    "force", "magic", "arcane" -> Color(0xFF00BCD4)
    "slashing", "piercing", "bludgeoning" -> Color(0xFF9E9E9E)
    else -> MaterialTheme.colorScheme.primary
}

@Composable
fun SkillIconBox(
    iconUrl: String?,
    tint: Color,
    bgTint: Color,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bgTint),
        contentAlignment = Alignment.Center,
    ) {
        if (!iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = tint,
            )
        }
    }
}
