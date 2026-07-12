package com.dnd.helper.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class CharacterStatsTest {

    @Test
    fun testAbilityModifier() {
        // Positive/Neutral scores
        assertEquals(0, abilityModifier(10))
        assertEquals(0, abilityModifier(11))
        assertEquals(1, abilityModifier(12))
        assertEquals(1, abilityModifier(13))
        assertEquals(5, abilityModifier(20))
        assertEquals(10, abilityModifier(30))

        // Negative scores (the bug fix)
        assertEquals(-1, abilityModifier(9))
        assertEquals(-1, abilityModifier(8))
        assertEquals(-2, abilityModifier(7))
        assertEquals(-2, abilityModifier(6))
        assertEquals(-3, abilityModifier(5))
        assertEquals(-5, abilityModifier(1))
        assertEquals(-5, abilityModifier(0))
    }

    @Test
    fun testCharacterStatsModifier() {
        val stats = CharacterStats(
            strength = 18,
            dexterity = 14,
            constitution = 12,
            intelligence = 8,
            wisdom = 10,
            charisma = 15,
        )

        assertEquals(4, stats.modifier("strength"))
        assertEquals(2, stats.modifier("dexterity"))
        assertEquals(1, stats.modifier("constitution"))
        assertEquals(-1, stats.modifier("intelligence"))
        assertEquals(0, stats.modifier("wisdom"))
        assertEquals(2, stats.modifier("charisma"))
        assertEquals(4, stats.modifier("STR")) // Strength is 18 -> modifier 4
        assertEquals(0, stats.modifier("unknown"))
    }
}
