package com.dnd.helper.domain.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testCharacterStatsSerialization() {
        val stats = CharacterStats(strength = 18, dexterity = 14)
        val encoded = json.encodeToString(stats)
        val decoded = json.decodeFromString<CharacterStats>(encoded)
        assertEquals(stats, decoded)
    }

    @Test
    fun testCharacterSerialization() {
        val character = Character(
            id = "test-id",
            name = "Test Hero",
            playerName = "Player",
            race = "Elf",
            characterClass = "Mage",
            level = 1,
            description = "Desc",
            maxHp = 10,
            currentHp = 10,
            stats = CharacterStats(strength = 15),
            combat = CharacterCombat(tempHp = 5),
            items = listOf(
                Item(
                    id = "item-1",
                    name = "Sword",
                    slot = EquipmentSlot.MAIN_HAND,
                    rarity = ItemRarity.COMMON
                )
            ),
            notes = listOf(
                Note(
                    id = "note-1",
                    title = "Quest",
                    content = "Find the ring"
                )
            )
        )
        val encoded = json.encodeToString(character)
        val decoded = json.decodeFromString<Character>(encoded)
        assertEquals(character, decoded)
    }
}
