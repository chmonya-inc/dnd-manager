package com.dnd.helper.presentation.characterdetail.spells

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.dnd.helper.di.isDesktop
import com.dnd.helper.domain.model.Spell
import com.dnd.helper.theme.DndIcons

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpellsTab(
    spells: List<Spell>,
    onEvent: (com.dnd.helper.presentation.characterdetail.CharacterDetailEvent) -> Unit = {},
    isMasterMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    var selectedSpell by remember { mutableStateOf<Spell?>(null) }

    selectedSpell?.let { spell ->
        // Find the latest version of this spell from the list to keep dialog in sync (e.g. during AI generation)
        val latestSpell = spells.find { it.id == spell.id } ?: spell
        
        SpellDetailDialog(
            spell = latestSpell,
            onDismiss = { selectedSpell = null },
            isMasterMode = isMasterMode,
            onDelete = {
                onEvent(com.dnd.helper.presentation.characterdetail.CharacterDetailEvent.RemoveSpell(spell.id))
                selectedSpell = null
            },
            onUpdate = { updatedSpell ->
                onEvent(com.dnd.helper.presentation.characterdetail.CharacterDetailEvent.UpdateSpell(updatedSpell))
            },
            onEvent = onEvent
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Spells (${spells.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (isMasterMode && isDesktop) {
                IconButton(onClick = { onEvent(com.dnd.helper.presentation.characterdetail.CharacterDetailEvent.AddSpell) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Spell")
                }
            }
        }

        if (spells.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No spells added", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val grouped = spells.groupBy { it.level }
                grouped.keys.sorted().forEach { level ->
                    item {
                        Text(
                            text = if (level == 0) "Cantrips" else "Level $level",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            grouped[level]?.forEach { spell ->
                                SpellCard(spell, onClick = { selectedSpell = spell })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpellCard(spell: Spell, onClick: () -> Unit) {
    val damageColor = getSpellDamageColor(spell.damageType)
    val isGenerating = spell.iconUrl?.startsWith("generating:") == true

    Card(
        modifier = Modifier
            .size(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, damageColor.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Icon/Image Background (Covers the card)
            if (isGenerating) {
                Box(
                    modifier = Modifier.fillMaxSize().background(damageColor.copy(alpha = 0.1f)), 
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            } else if (!spell.displayIconUrl.isNullOrBlank()) {
                AsyncImage(
                    model = spell.displayIconUrl,
                    contentDescription = spell.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop, // This makes it cover the card
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(damageColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = DndIcons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = damageColor.copy(alpha = 0.6f),
                    )
                }
            }
            
            // 2. Text Overlay on Black Background
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Text(
                    text = spell.name,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun SpellInfo(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
