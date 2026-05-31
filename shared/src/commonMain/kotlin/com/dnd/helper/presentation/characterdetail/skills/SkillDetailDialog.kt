package com.dnd.helper.presentation.characterdetail.skills

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
    isMasterMode: Boolean = false,
    onDelete: () -> Unit = {}
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
                            iconUrl = skill.displayIconUrl,
                            tint = getSkillDamageColor(skill.damageType),
                            bgTint = getSkillDamageColor(skill.damageType).copy(alpha = 0.15f),
                            size = 78.dp,
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
                        SkillTag(skill.damageType, getSkillDamageColor(skill.damageType).copy(alpha = 0.15f))
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
                        valueColor = getSkillDamageColor(skill.damageType),
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

                if (isMasterMode) {
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete Skill")
                    }
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
fun getSkillDamageColor(type: String): Color = when (type.lowercase()) {
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .size(size),
        shape = RoundedCornerShape(6.dp)
    ) {
        if (!iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Fit,
            )
        } else {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(size),
                tint = tint,
            )
        }
    }
}
