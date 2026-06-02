package com.dnd.helper.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Centralized semantic color tokens for the D&D Helper app.
 *
 * These replace the previously duplicated hardcoded colors across
 * LibraryScreen, CreatorScreen, PresentationScreen, and MasterCharacterDetailScreen.
 * Provided via [LocalDndColors] and adapt per-theme.
 */
@Immutable
data class DndColors(
    // --- Entity Category Colors ---
    val character: Color,
    val monster: Color,
    val npc: Color,
    val location: Color,
    val battlefield: Color,
    val item: Color,

    // --- Section Colors (MasterCharacterDetail) ---
    val stats: Color,
    val skills: Color,
    val combat: Color,
    val inventory: Color,
    val features: Color,
    val notes: Color,

    // --- HP Status Colors ---
    val hpHealthy: Color,
    val hpDamaged: Color,
    val hpCritical: Color,

    // --- Ability Score Colors ---
    val strength: Color,
    val dexterity: Color,
    val constitution: Color,
    val intelligence: Color,
    val wisdom: Color,
    val charisma: Color,

    // --- Diff Colors (LogScreen) ---
    val diffAdded: Color,
    val diffRemoved: Color,

    // --- Rarity Colors ---
    val rarityCommon: Color,
    val rarityUncommon: Color,
    val rarityRare: Color,
    val rarityEpic: Color,
    val rarityLegendary: Color,
)

// ---- Default palettes per theme ----

val DungeonDndColors = DndColors(
    // Entity categories
    character = Color(0xFFAB47BC),
    monster = Color(0xFFEF5350),
    npc = Color(0xFF66BB6A),
    location = Color(0xFF42A5F5),
    battlefield = Color(0xFF009688),
    item = Color(0xFFFFA726),
    // Sections
    stats = Color(0xFFEF5350),
    skills = Color(0xFF66BB6A),
    combat = Color(0xFF42A5F5),
    inventory = Color(0xFFFFA726),
    features = Color(0xFFAB47BC),
    notes = Color(0xFF795548),
    // HP
    hpHealthy = Color(0xFF43A047),
    hpDamaged = Color(0xFFFFA726),
    hpCritical = Color(0xFFD32F2F),
    // Abilities
    strength = Color(0xFFEF5350),
    dexterity = Color(0xFF66BB6A),
    constitution = Color(0xFFFFA726),
    intelligence = Color(0xFF42A5F5),
    wisdom = Color(0xFFAB47BC),
    charisma = Color(0xFFEC407A),
    // Diff
    diffAdded = Color(0xFF388E3C),
    diffRemoved = Color(0xFFD32F2F),
    // Rarity
    rarityCommon = Color(0xFF9E9E9E),
    rarityUncommon = Color(0xFF43A047),
    rarityRare = Color(0xFF1E88E5),
    rarityEpic = Color(0xFF8E24AA),
    rarityLegendary = Color(0xFFFB8C00),
)

val ParchmentDndColors = DungeonDndColors.copy(
    // Slightly warmer / muted variants for light parchment theme
    character = Color(0xFF7B1FA2),
    monster = Color(0xFFC62828),
    npc = Color(0xFF2E7D32),
    location = Color(0xFF1565C0),
    battlefield = Color(0xFF00695C),
    item = Color(0xFFEF6C00),
    stats = Color(0xFFC62828),
    skills = Color(0xFF2E7D32),
    combat = Color(0xFF1565C0),
    inventory = Color(0xFFEF6C00),
    features = Color(0xFF7B1FA2),
    notes = Color(0xFF5D4037),
)

val ForestDndColors = DungeonDndColors.copy(
    character = Color(0xFFCE93D8),
    monster = Color(0xFFEF9A9A),
    npc = Color(0xFFA5D6A7),
    location = Color(0xFF90CAF9),
    item = Color(0xFFFFCC80),
    stats = Color(0xFFEF9A9A),
    skills = Color(0xFFA5D6A7),
)

val BloodDndColors = DungeonDndColors.copy(
    character = Color(0xFFCE93D8),
    monster = Color(0xFFFF5252),
    npc = Color(0xFF69F0AE),
    location = Color(0xFF448AFF),
    item = Color(0xFFFFAB40),
    hpHealthy = Color(0xFF00E676),
    hpCritical = Color(0xFFFF1744),
)

val CelestialDndColors = DungeonDndColors.copy(
    character = Color(0xFFE1BEE7),
    monster = Color(0xFFEF9A9A),
    npc = Color(0xFFA5D6A7),
    location = Color(0xFF90CAF9),
    item = Color(0xFFFFE082),
    stats = Color(0xFFEF9A9A),
    skills = Color(0xFFA5D6A7),
)

/**
 * CompositionLocal providing the current [DndColors] tokens.
 *
 * Usage: `LocalDndColors.current.monster` instead of hardcoded `Color(0xFFEF5350)`.
 */
val LocalDndColors = staticCompositionLocalOf { DungeonDndColors }

/**
 * Convenience accessor for the current DndColors in a @Composable scope.
 */
val dndColors: DndColors
    @Composable get() = LocalDndColors.current
