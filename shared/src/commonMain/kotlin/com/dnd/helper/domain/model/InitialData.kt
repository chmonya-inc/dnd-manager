package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InitialData(
    val characters: List<Character>,
    val locations: List<Location>,
    val monsters: List<Monster>,
    val npcs: List<Npc>,
    val music: List<MusicTrack> = emptyList(),
    val events: List<GameEvent> = emptyList(),
    val lastModified: String
)

@Serializable
data class GameEvent(
    val id: String,
    val name: String,
    val items: List<PresentedItem>
)

@Serializable
data class PresentedItem(
    val id: String,
    val sourceId: String? = null, // The real ID (Character ID, Monster ID, etc.)
    val title: String,
    val type: String,
    val imageUrl: String? = null,
    // x, y, width, height are all in logical units (0 to 1000)
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 200f,
    val height: Float = 200f,
    val isBackground: Boolean = false,

    // Stats for display
    val currentHp: Int? = null,
    val maxHp: Int? = null,
    val armorClass: Int? = null,
    val stats: CharacterStats? = null,
    val subInfo: String? = null, // e.g. "CR 1/2", "Humanoid"
    val description: String? = null,
    // Zoom and Pan for background locations
    val zoom: Float = 1.0f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f
)
