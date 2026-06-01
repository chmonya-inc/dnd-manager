package com.dnd.helper.presentation.characterdetail.combat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.Weapon

@Composable
fun CombatTab(
    character: Character,
    isMasterMode: Boolean = false,
    isScrollable: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .then(if (isScrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Weapons & Attacks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (isMasterMode) {
                IconButton(onClick = { /* TODO: onEvent(CharacterDetailEvent.ShowAddWeaponDialog) */ }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Weapon")
                }
            }
        }

        if (character.weapons.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No weapons added",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            character.weapons.forEach { weapon ->
                WeaponCard(weapon, isMasterMode)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Combat notes / description
        if (character.description.isNotBlank()) {
            Text(
                text = "Combat Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = character.description,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun WeaponCard(
    weapon: Weapon,
    isMasterMode: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weapon.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (isMasterMode) {
                    IconButton(onClick = { /* TODO: onEvent(CharacterDetailEvent.DeleteWeapon(weapon.id)) */ }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Weapon",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeaponStat("Attack", weapon.attackBonus.ifBlank { "+0" })
                WeaponStat("Damage", weapon.damage.ifBlank { "—" })
                if (weapon.damageType.isNotBlank()) {
                    WeaponStat("Type", weapon.damageType)
                }
            }

            if (weapon.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = weapon.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeaponStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
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
