package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Skill(
    val id: String,
    val name: String,
    val description: String = "",
    val iconUrl: String? = null,         // Image URL for skill icon (like character imageUrl)
    val damage: String = "",             // e.g. "2d6+3", "1d8"
    val damageType: String = "",         // e.g. "Fire", "Cold", "Necrotic", "Slashing"
    val resourceCost: String = "",       // e.g. "1 Action", "Bonus Action", "Reaction", "10 Mana"
    val range: String = "",              // e.g. "60 ft", "Touch", "Self", "120 ft"
    val castingTime: String = "",        // e.g. "1 Action", "1 Bonus Action"
    val duration: String = "",           // e.g. "Instantaneous", "1 minute", "Concentration, up to 10 min"
    val level: Int = 0,                  // Spell level or feature level (0 = cantrip / at-will)
    val school: String = "",             // e.g. "Evocation", "Abjuration", "Transmutation"
    val isPassive: Boolean = false,
)
