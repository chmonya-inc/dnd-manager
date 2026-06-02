package com.dnd.helper.presentation.characterdetail.skills

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import com.dnd.helper.data.remote.AiImageService
import com.dnd.helper.data.remote.GenerationType
import com.dnd.helper.data.remote.PromptGenerator
import com.dnd.helper.domain.model.Skill
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun SkillDetailDialog(
    skill: Skill,
    onDismiss: () -> Unit,
    isMasterMode: Boolean = false,
    onDelete: () -> Unit = {},
    onUpdate: (Skill) -> Unit = {}
) {
    val aiService: AiImageService = koinInject()
    val scope = rememberCoroutineScope()
    var editedSkill by remember { mutableStateOf(skill) }
    var customPrompt by remember { mutableStateOf(PromptGenerator.getFullPrompt("${editedSkill.name}, ${editedSkill.damageType} skill. ${editedSkill.description}", GenerationType.SKILL)) }
    var aiWidth by remember { mutableIntStateOf(256) }
    var aiHeight by remember { mutableIntStateOf(256) }
    var isGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(editedSkill.name, editedSkill.damageType, editedSkill.description) {
        customPrompt = PromptGenerator.getFullPrompt("${editedSkill.name}, ${editedSkill.damageType} skill. ${editedSkill.description}".trim(), GenerationType.SKILL)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 700.dp),
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
                            iconUrl = editedSkill.displayIconUrl,
                            tint = getSkillDamageColor(editedSkill.damageType),
                            bgTint = getSkillDamageColor(editedSkill.damageType).copy(alpha = 0.15f),
                            size = 78.dp,
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            if (isMasterMode) {
                                OutlinedTextField(
                                    value = editedSkill.name,
                                    onValueChange = { 
                                        editedSkill = editedSkill.copy(name = it)
                                        onUpdate(editedSkill)
                                    },
                                    label = { Text("Name") }
                                )
                            } else {
                                Text(
                                    text = editedSkill.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (editedSkill.school.isNotBlank()) {
                                    Text(
                                        text = editedSkill.school,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                if (isMasterMode) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = editedSkill.name, onValueChange = { editedSkill = editedSkill.copy(name = it); onUpdate(editedSkill) }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSkill.iconUrl ?: "", onValueChange = { editedSkill = editedSkill.copy(iconUrl = it.ifBlank { null }); onUpdate(editedSkill) }, label = { Text("Icon URL") }, modifier = Modifier.fillMaxWidth())
                        
                        // AI Generation Section
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("AI Icon Generation (${aiWidth}x${aiHeight})", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = customPrompt,
                                        onValueChange = { customPrompt = it },
                                        label = { Text("AI Prompt") },
                                        modifier = Modifier.weight(1f),
                                        minLines = 2,
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        OutlinedTextField(
                                            value = aiWidth.toString(),
                                            onValueChange = { aiWidth = it.toIntOrNull() ?: aiWidth },
                                            label = { Text("W") },
                                            modifier = Modifier.width(70.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(8.dp),
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )
                                        OutlinedTextField(
                                            value = aiHeight.toString(),
                                            onValueChange = { aiHeight = it.toIntOrNull() ?: aiHeight },
                                            label = { Text("H") },
                                            modifier = Modifier.width(70.dp),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(8.dp),
                                            textStyle = MaterialTheme.typography.bodySmall
                                        )
                                        IconButton(
                                            onClick = { 
                                                isGenerating = true
                                                editedSkill = editedSkill.copy(iconUrl = "url will appear after generation")
                                                scope.launch {
                                                    val url = aiService.generateImage(customPrompt, GenerationType.SKILL, aiWidth, aiHeight)
                                                    if (url != null) {
                                                        editedSkill = editedSkill.copy(iconUrl = url)
                                                        onUpdate(editedSkill)
                                                    } else {
                                                        editedSkill = editedSkill.copy(iconUrl = "")
                                                    }
                                                    isGenerating = false
                                                }
                                            },
                                            enabled = !isGenerating
                                        ) {
                                            if (isGenerating) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                            } else {
                                                Icon(Icons.Default.AutoAwesome, "Generate Icon", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(value = editedSkill.level.toString(), onValueChange = { editedSkill = editedSkill.copy(level = it.toIntOrNull() ?: 0); onUpdate(editedSkill) }, label = { Text("Level") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = editedSkill.damageType, onValueChange = { editedSkill = editedSkill.copy(damageType = it); onUpdate(editedSkill) }, label = { Text("Damage Type") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSkill.damage, onValueChange = { editedSkill = editedSkill.copy(damage = it); onUpdate(editedSkill) }, label = { Text("Damage") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSkill.resourceCost, onValueChange = { editedSkill = editedSkill.copy(resourceCost = it); onUpdate(editedSkill) }, label = { Text("Cost") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSkill.castingTime, onValueChange = { editedSkill = editedSkill.copy(castingTime = it); onUpdate(editedSkill) }, label = { Text("Casting") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSkill.range, onValueChange = { editedSkill = editedSkill.copy(range = it); onUpdate(editedSkill) }, label = { Text("Range") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSkill.duration, onValueChange = { editedSkill = editedSkill.copy(duration = it); onUpdate(editedSkill) }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSkill.description, onValueChange = { editedSkill = editedSkill.copy(description = it); onUpdate(editedSkill) }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    }
                } else {
                    // Tags row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (editedSkill.level > 0) {
                            SkillTag("Level ${editedSkill.level}", MaterialTheme.colorScheme.primaryContainer)
                        } else if (editedSkill.isPassive) {
                            SkillTag("Passive", MaterialTheme.colorScheme.tertiaryContainer)
                        } else {
                            SkillTag("Cantrip", MaterialTheme.colorScheme.secondaryContainer)
                        }
                        if (editedSkill.damageType.isNotBlank()) {
                            SkillTag(editedSkill.damageType, getSkillDamageColor(editedSkill.damageType).copy(alpha = 0.15f))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Detail fields
                    if (editedSkill.resourceCost.isNotBlank()) {
                        DetailRow(label = "Cost", value = editedSkill.resourceCost)
                    }
                    if (editedSkill.castingTime.isNotBlank()) {
                        DetailRow(label = "Casting", value = editedSkill.castingTime)
                    }
                    if (editedSkill.range.isNotBlank()) {
                        DetailRow(label = "Range", value = editedSkill.range)
                    }
                    if (editedSkill.duration.isNotBlank()) {
                        DetailRow(label = "Duration", value = editedSkill.duration)
                    }
                    if (editedSkill.damage.isNotBlank()) {
                        DetailRow(
                            label = "Damage",
                            value = editedSkill.damage,
                            valueColor = getSkillDamageColor(editedSkill.damageType),
                        )
                    }

                    if (editedSkill.description.isNotBlank()) {
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
                            text = editedSkill.description,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
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
                modifier = Modifier.size(size).align(Alignment.CenterHorizontally),
                tint = tint,
            )
        }
    }
}
