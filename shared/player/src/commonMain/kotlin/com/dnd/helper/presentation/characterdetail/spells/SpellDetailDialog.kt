package com.dnd.helper.presentation.characterdetail.spells

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
import com.dnd.helper.domain.model.Spell
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun SpellDetailDialog(
    spell: Spell,
    onDismiss: () -> Unit,
    isMasterMode: Boolean = false,
    onDelete: () -> Unit = {},
    onUpdate: (Spell) -> Unit = {},
    onEvent: (com.dnd.helper.presentation.characterdetail.CharacterDetailEvent) -> Unit = {}
) {
    var editedSpell by remember { mutableStateOf(spell) }
    
    // Sync editedSpell when spell from state changes (e.g. after generation completes)
    LaunchedEffect(spell) {
        editedSpell = spell
    }

    // Check if generating
    val isGenerating = editedSpell.iconUrl?.startsWith("generating:") == true
    
    var customPrompt by remember { mutableStateOf(PromptGenerator.getFullPrompt("${editedSpell.name}, ${editedSpell.school} spell. ${editedSpell.description}", GenerationType.SKILL)) }

    LaunchedEffect(editedSpell.name, editedSpell.school, editedSpell.description) {
        customPrompt = PromptGenerator.getFullPrompt("${editedSpell.name}, ${editedSpell.school} spell. ${editedSpell.description}".trim(), GenerationType.SKILL)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 700.dp),
            shape = MaterialTheme.shapes.large,
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
                        SpellIconBox(
                            iconUrl = editedSpell.displayIconUrl,
                            tint = getSpellDamageColor(editedSpell.damageType),
                            bgTint = getSpellDamageColor(editedSpell.damageType).copy(alpha = 0.15f),
                            size = 78.dp,
                            isGenerating = isGenerating
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            if (isMasterMode) {
                                OutlinedTextField(
                                    value = editedSpell.name,
                                    onValueChange = { 
                                        editedSpell = editedSpell.copy(name = it)
                                        onUpdate(editedSpell)
                                    },
                                    label = { Text("Name") }
                                )
                            } else {
                                Text(
                                    text = editedSpell.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                if (editedSpell.school.isNotBlank()) {
                                    Text(
                                        text = editedSpell.school,
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
                        OutlinedTextField(value = editedSpell.iconUrl ?: "", onValueChange = { editedSpell = editedSpell.copy(iconUrl = it.ifBlank { null }); onUpdate(editedSpell) }, label = { Text("Icon URL") }, modifier = Modifier.fillMaxWidth())
                        
                        // AI Generation Section
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("AI Icon Generation", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = customPrompt,
                                        onValueChange = { customPrompt = it },
                                        label = { Text("AI Prompt") },
                                        modifier = Modifier.weight(1f),
                                        minLines = 2,
                                        shape = MaterialTheme.shapes.medium,
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { 
                                                onEvent(com.dnd.helper.presentation.characterdetail.CharacterDetailEvent.GenerateSpellImage(editedSpell.id))
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

                        OutlinedTextField(value = editedSpell.level.toString(), onValueChange = { editedSpell = editedSpell.copy(level = it.toIntOrNull() ?: 0); onUpdate(editedSpell) }, label = { Text("Level") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = editedSpell.damageType, onValueChange = { editedSpell = editedSpell.copy(damageType = it); onUpdate(editedSpell) }, label = { Text("Damage Type") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSpell.damage, onValueChange = { editedSpell = editedSpell.copy(damage = it); onUpdate(editedSpell) }, label = { Text("Damage") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSpell.resourceCost, onValueChange = { editedSpell = editedSpell.copy(resourceCost = it); onUpdate(editedSpell) }, label = { Text("Cost") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSpell.castingTime, onValueChange = { editedSpell = editedSpell.copy(castingTime = it); onUpdate(editedSpell) }, label = { Text("Casting") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSpell.range, onValueChange = { editedSpell = editedSpell.copy(range = it); onUpdate(editedSpell) }, label = { Text("Range") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSpell.duration, onValueChange = { editedSpell = editedSpell.copy(duration = it); onUpdate(editedSpell) }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = editedSpell.description, onValueChange = { editedSpell = editedSpell.copy(description = it); onUpdate(editedSpell) }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                    }
                } else {
                    // Simplified View for Players
                    if (editedSpell.school.isNotBlank()) {
                        SpellTag(editedSpell.school, getSpellDamageColor(editedSpell.damageType).copy(alpha = 0.15f))
                        Spacer(Modifier.height(12.dp))
                    }

                    if (editedSpell.damage.isNotBlank()) {
                        DetailRow(
                            label = "Damage",
                            value = editedSpell.damage,
                            valueColor = getSpellDamageColor(editedSpell.damageType),
                        )
                    }

                    if (editedSpell.description.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = editedSpell.description,
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
                        Text("Delete Spell")
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellTag(text: String, bgColor: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
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
fun getSpellDamageColor(type: String): Color = when (type.lowercase()) {
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
fun SpellIconBox(
    iconUrl: String?,
    tint: Color,
    bgTint: Color,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    isGenerating: Boolean = false,
) {
    Card(
        modifier = modifier
            .size(size),
        shape = MaterialTheme.shapes.small
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isGenerating) {
                CircularProgressIndicator(modifier = Modifier.size(size * 0.4f), strokeWidth = 2.dp)
            } else if (!iconUrl.isNullOrBlank()) {
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
                    modifier = Modifier.size(size * 0.6f),
                    tint = tint,
                )
            }
        }
    }
}
