package com.dnd.helper.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class AppTheme(val displayName: String) {
    DUNGEON("Dungeon Deep"),
    PARCHMENT("Old Parchment"),
    FOREST("Emerald Forest"),
    BLOOD("Blood & Iron"),
    CELESTIAL("Celestial Holy")
}

private val DungeonShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)

private val ParchmentShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp)
)

private val ForestShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(32.dp)
)

private val BloodShapes = Shapes(
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp)
)

private val CelestialShapes = Shapes(
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(40.dp)
)

private val DungeonColorScheme = darkColorScheme(
    primary = Primary80,
    onPrimary = Primary20,
    primaryContainer = Primary30,
    onPrimaryContainer = Primary90,
    secondary = Secondary80,
    onSecondary = Secondary20,
    secondaryContainer = Secondary30,
    onSecondaryContainer = Secondary90,
    surface = Neutral06,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral80,
    background = Neutral06,
    onBackground = Neutral90,
    outline = Neutral60,
    error = Error80,
)

private val ParchmentColorScheme = lightColorScheme(
    primary = ParchmentPrimary,
    onPrimary = Color.White,
    secondary = ParchmentSecondary,
    surface = ParchmentSurface,
    onSurface = ParchmentOnSurface,
    background = ParchmentSurface,
    onBackground = ParchmentOnSurface,
    surfaceVariant = Color(0xFFE5D8BC),
    outline = Color(0xFFBCAAA4),
)

private val ForestColorScheme = darkColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    secondary = ForestSecondary,
    surface = ForestSurface,
    onSurface = ForestOnSurface,
    background = ForestSurface,
    onBackground = ForestOnSurface,
)

private val BloodColorScheme = darkColorScheme(
    primary = BloodPrimary,
    onPrimary = Color.White,
    secondary = BloodSecondary,
    surface = BloodSurface,
    onSurface = BloodOnSurface,
    background = BloodSurface,
    onBackground = BloodOnSurface,
)

private val CelestialColorScheme = darkColorScheme(
    primary = HolyPrimary,
    onPrimary = Color.Black,
    secondary = HolySecondary,
    surface = HolySurface,
    onSurface = HolyOnSurface,
    background = HolySurface,
    onBackground = HolyOnSurface,
)

val LocalAppTheme = staticCompositionLocalOf { AppTheme.DUNGEON }

@Composable
fun DndHelperTheme(
    theme: AppTheme = AppTheme.DUNGEON,
    content: @Composable () -> Unit,
) {
    val colorScheme = when (theme) {
        AppTheme.DUNGEON -> DungeonColorScheme
        AppTheme.PARCHMENT -> ParchmentColorScheme
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.BLOOD -> BloodColorScheme
        AppTheme.CELESTIAL -> CelestialColorScheme
    }
    
    val shapes = when (theme) {
        AppTheme.DUNGEON -> DungeonShapes
        AppTheme.PARCHMENT -> ParchmentShapes
        AppTheme.FOREST -> ForestShapes
        AppTheme.BLOOD -> BloodShapes
        AppTheme.CELESTIAL -> CelestialShapes
    }

    val dndColors = when (theme) {
        AppTheme.DUNGEON -> DungeonDndColors
        AppTheme.PARCHMENT -> ParchmentDndColors
        AppTheme.FOREST -> ForestDndColors
        AppTheme.BLOOD -> BloodDndColors
        AppTheme.CELESTIAL -> CelestialDndColors
    }

    CompositionLocalProvider(
        LocalAppTheme provides theme,
        LocalDndColors provides dndColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = shapes,
            content = content,
        )
    }
}
