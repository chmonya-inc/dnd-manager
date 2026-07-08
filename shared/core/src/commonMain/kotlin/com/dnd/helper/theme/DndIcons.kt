package com.dnd.helper.theme


import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object DndIcons {
    object Filled {
        private fun buildIcon(
            name: String,
            block: ImageVector.Builder.() -> ImageVector.Builder
        ): ImageVector = ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).block().build()

        val Inventory: ImageVector = buildIcon("Filled.Inventory") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 2f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(16f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(16f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(4f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(20f, 20f); horizontalLineTo(4f); verticalLineTo(4f); horizontalLineToRelative(16f); verticalLineToRelative(16f); close()
                moveTo(15f, 7.5f); horizontalLineTo(9f); verticalLineTo(9f); horizontalLineToRelative(6f); verticalLineTo(7.5f); close()
                moveTo(15f, 11f); horizontalLineTo(9f); verticalLineToRelative(1.5f); horizontalLineToRelative(6f); verticalLineTo(11f); close()
                moveTo(15f, 14.5f); horizontalLineTo(9f); verticalLineTo(16f); horizontalLineToRelative(6f); verticalLineToRelative(-1.5f); close()
            }
        }

        val BugReport: ImageVector = buildIcon("Filled.BugReport") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 8f); horizontalLineToRelative(-2.81f); curveToRelative(-0.45f, -0.78f, -1.07f, -1.45f, -1.82f, -1.96f); lineToRelative(1.14f, -1.14f); lineToRelative(-1.41f, -1.41f); lineToRelative(-1.55f, 1.56f); curveTo(13.56f, 5.02f, 12.79f, 5f, 12f, 5f); reflectiveCurveToRelative(-1.56f, 0.02f, -2.35f, 0.06f); lineTo(8.09f, 3.49f); lineTo(6.68f, 4.9f); lineToRelative(1.14f, 1.14f); curveTo(7.07f, 6.55f, 6.45f, 7.22f, 6f, 8f); horizontalLineTo(4f); verticalLineToRelative(2f); horizontalLineTo(6.09f); curveToRelative(-0.05f, 0.33f, -0.09f, 0.66f, -0.09f, 1f); verticalLineToRelative(1f); horizontalLineTo(4f); verticalLineToRelative(2f); horizontalLineToRelative(2f); verticalLineToRelative(1f); curveToRelative(0f, 0.34f, 0.04f, 0.67f, 0.09f, 1f); horizontalLineTo(4f); verticalLineToRelative(2f); horizontalLineToRelative(2.81f); curveToRelative(1.04f, 1.79f, 2.97f, 3f, 5.19f, 3f); reflectiveCurveToRelative(4.15f, -1.21f, 5.19f, -3f); horizontalLineTo(20f); verticalLineToRelative(-2f); horizontalLineToRelative(-2.09f); curveToRelative(0.05f, -0.33f, 0.09f, -0.66f, 0.09f, -1f); verticalLineToRelative(-1f); horizontalLineToRelative(2f); verticalLineToRelative(-2f); horizontalLineToRelative(-2f); verticalLineToRelative(-1f); curveToRelative(0f, -0.34f, -0.04f, -0.67f, -0.09f, -1f); horizontalLineTo(20f); verticalLineTo(8f); close()
                moveTo(14f, 16f); horizontalLineToRelative(-4f); verticalLineToRelative(-2f); horizontalLineToRelative(4f); verticalLineToRelative(2f); close()
                moveTo(14f, 12f); horizontalLineToRelative(-4f); verticalLineToRelative(-2f); horizontalLineToRelative(4f); verticalLineToRelative(2f); close()
            }
        }

        val EmojiPeople: ImageVector = buildIcon("Filled.EmojiPeople") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(15.89f, 8.11f); lineToRelative(-2.53f, 2.53f); verticalLineTo(7f); curveToRelative(0f, -0.55f, -0.45f, -1f, -1f, -1f); horizontalLineTo(9f); curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f); verticalLineToRelative(6.44f); lineToRelative(-1.59f, 1.59f); curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f); reflectiveCurveToRelative(1.02f, 0.39f, 1.41f, 0f); lineTo(10f, 15.41f); verticalLineTo(20f); curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f); reflectiveCurveToRelative(1f, -0.45f, 1f, -1f); verticalLineToRelative(-5f); horizontalLineToRelative(2f); verticalLineTo(20f); curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f); reflectiveCurveToRelative(1f, -0.45f, 1f, -1f); verticalLineToRelative(-4.59f); lineToRelative(1.12f, -1.12f); curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f); reflectiveCurveToRelative(-1.02f, -0.39f, -1.41f, 0f); close()
            }
        }

        val Map: ImageVector = buildIcon("Filled.Map") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.5f, 3f); lineToRelative(-0.16f, 0.03f); lineTo(15f, 5.1f); lineTo(9f, 3f); lineTo(3.36f, 4.9f); curveToRelative(-0.21f, 0.07f, -0.36f, 0.25f, -0.36f, 0.48f); verticalLineTo(20.5f); curveToRelative(0f, 0.28f, 0.22f, 0.5f, 0.5f, 0.5f); lineToRelative(0.16f, -0.03f); lineTo(9f, 18.9f); lineToRelative(6f, 2.1f); lineToRelative(5.64f, -1.9f); curveToRelative(0.21f, -0.07f, 0.36f, -0.25f, 0.36f, -0.48f); verticalLineTo(3.5f); curveToRelative(0f, -0.28f, -0.22f, -0.5f, -0.5f, -0.5f); close()
                moveTo(15f, 19f); lineToRelative(-6f, -2.11f); verticalLineTo(5f); lineToRelative(6f, 2.11f); verticalLineTo(19f); close()
            }
        }

        val AutoAwesome: ImageVector = buildIcon("Filled.AutoAwesome") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 9f); lineToRelative(1.25f, -2.75f); lineTo(23f, 5f); lineToRelative(-2.75f, -1.25f); lineTo(19f, 1f); lineToRelative(-1.25f, 2.75f); lineTo(15f, 5f); lineToRelative(2.75f, 1.25f); lineTo(19f, 9f); close()
                moveTo(11.5f, 9.5f); lineTo(9f, 4f); lineTo(6.5f, 9.5f); lineTo(1f, 12f); lineToRelative(5.5f, 2.5f); lineTo(9f, 20f); lineToRelative(2.5f, -5.5f); lineTo(17f, 12f); lineToRelative(-5.5f, -2.5f); close()
                moveTo(19f, 15f); lineToRelative(-1.25f, 2.75f); lineTo(15f, 19f); lineToRelative(2.75f, 1.25f); lineTo(19f, 23f); lineToRelative(1.25f, -2.75f); lineTo(23f, 19f); lineToRelative(-2.75f, -1.25f); lineTo(19f, 15f); close()
            }
        }

        val ShoppingBag: ImageVector = buildIcon("Filled.ShoppingBag") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 6f); horizontalLineToRelative(-2f); curveToRelative(0f, -2.21f, -1.79f, -4f, -4f, -4f); reflectiveCurveTo(8f, 3.79f, 8f, 6f); horizontalLineTo(6f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(12f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(8f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(12f, 4f); curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f); horizontalLineTo(10f); curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f); close()
                moveTo(18f, 20f); horizontalLineTo(6f); verticalLineTo(8f); horizontalLineToRelative(2f); verticalLineToRelative(2f); curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f); reflectiveCurveToRelative(1f, -0.45f, 1f, -1f); verticalLineTo(8f); horizontalLineToRelative(4f); verticalLineToRelative(2f); curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f); reflectiveCurveToRelative(1f, -0.45f, 1f, -1f); verticalLineTo(8f); horizontalLineToRelative(2f); verticalLineToRelative(12f); close()
            }
        }

        val Remove: ImageVector = buildIcon("Filled.Remove") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 13f); horizontalLineTo(5f); verticalLineToRelative(-2f); horizontalLineToRelative(14f); verticalLineToRelative(2f); close()
            }
        }

        val RadioButtonUnchecked: ImageVector = buildIcon("Filled.RadioButtonUnchecked") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); reflectiveCurveToRelative(4.48f, 10f, 10f, 10f); reflectiveCurveToRelative(10f, -4.48f, 10f, -10f); reflectiveCurveTo(17.52f, 2f, 12f, 2f); close()
                moveTo(12f, 20f); curveToRelative(-4.42f, 0f, -8f, -3.58f, -8f, -8f); reflectiveCurveToRelative(3.58f, -8f, 8f, -8f); reflectiveCurveToRelative(8f, 3.58f, 8f, 8f); reflectiveCurveToRelative(-3.58f, 8f, -8f, 8f); close()
            }
        }

        val CheckCircle: ImageVector = buildIcon("Filled.CheckCircle") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); reflectiveCurveToRelative(4.48f, 10f, 10f, 10f); reflectiveCurveToRelative(10f, -4.48f, 10f, -10f); reflectiveCurveTo(17.52f, 2f, 12f, 2f); close()
                moveTo(10f, 17f); lineToRelative(-5f, -5f); lineToRelative(1.41f, -1.41f); lineTo(10f, 14.17f); lineToRelative(7.59f, -7.59f); lineTo(19f, 8f); lineToRelative(-9f, 9f); close()
            }
        }

        val Tv: ImageVector = buildIcon("Filled.Tv") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 3f); lineTo(3f, 3f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(5f); verticalLineToRelative(2f); horizontalLineToRelative(8f); verticalLineToRelative(-2f); horizontalLineToRelative(5f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(5f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(21f, 17f); horizontalLineTo(3f); verticalLineTo(5f); horizontalLineToRelative(18f); verticalLineToRelative(12f); close()
            }
        }

        val TvOff: ImageVector = buildIcon("Filled.TvOff") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 3f); lineTo(3f, 3f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(5f); verticalLineToRelative(2f); horizontalLineToRelative(8f); verticalLineToRelative(-2f); horizontalLineToRelative(5f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(5f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(2.41f, 2.13f); lineTo(1.11f, 3.44f); lineToRelative(1.61f, 1.61f); lineTo(3f, 5.05f); verticalLineTo(17f); horizontalLineToRelative(11.95f); lineToRelative(2.15f, 2.15f); horizontalLineTo(16f); verticalLineToRelative(2f); horizontalLineTo(8f); verticalLineToRelative(-2f); horizontalLineTo(3f); curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f); verticalLineTo(5f); curveToRelative(0f, -0.67f, 0.33f, -1.26f, 0.84f, -1.63f); lineTo(2.41f, 2.13f); close()
                moveTo(21f, 17f); horizontalLineToRelative(-3.17f); lineToRelative(1.17f, 1.17f); curveToRelative(0.51f, -0.37f, 0.84f, -0.96f, 0.84f, -1.63f); verticalLineTo(5f); horizontalLineTo(8.17f); lineToRelative(2f, 2f); horizontalLineTo(21f); verticalLineToRelative(10f); close()
            }
        }

        val MusicNote: ImageVector = buildIcon("Filled.MusicNote") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f); verticalLineToRelative(10.55f); curveToRelative(-0.59f, -0.34f, -1.27f, -0.55f, -2f, -0.55f); curveToRelative(-2.21f, 0f, -4f, 1.79f, -4f, 4f); reflectiveCurveToRelative(1.79f, 4f, 4f, 4f); reflectiveCurveToRelative(4f, -1.79f, 4f, -4f); verticalLineTo(7f); horizontalLineToRelative(4f); verticalLineTo(3f); horizontalLineToRelative(-4f); close()
            }
        }

        val MusicOff: ImageVector = buildIcon("Filled.MusicOff") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4.27f, 3f); lineTo(3f, 4.27f); lineToRelative(9f, 9f); verticalLineToRelative(0.28f); curveToRelative(-0.59f, -0.34f, -1.27f, -0.55f, -2f, -0.55f); curveToRelative(-2.21f, 0f, -4f, 1.79f, -4f, 4f); reflectiveCurveToRelative(1.79f, 4f, 4f, 4f); reflectiveCurveToRelative(4f, -1.79f, 4f, -4f); verticalLineToRelative(-1.73f); lineTo(19.73f, 21f); lineTo(21f, 19.73f); lineTo(4.27f, 3f); close()
                moveTo(14f, 7f); horizontalLineToRelative(4f); verticalLineTo(3f); horizontalLineToRelative(-6f); verticalLineToRelative(5.18f); lineToRelative(2f, 2f); verticalLineTo(7f); close()
            }
        }

        val AutoFixHigh: ImageVector = buildIcon("Filled.AutoFixHigh") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 7f); lineToRelative(0.94f, -2.06f); lineTo(23f, 4f); lineToRelative(-2.06f, -0.94f); lineTo(20f, 1f); lineToRelative(-0.94f, 2.06f); lineTo(17f, 4f); lineToRelative(2.06f, 0.94f); lineTo(20f, 7f); close()
                moveTo(8.5f, 7f); lineToRelative(0.94f, -2.06f); lineTo(11.5f, 4f); lineToRelative(-2.06f, -0.94f); lineTo(8.5f, 1f); lineToRelative(-0.94f, 2.06f); lineTo(5.5f, 4f); lineToRelative(2.06f, 0.94f); lineTo(8.5f, 7f); close()
                moveTo(20f, 12.5f); lineToRelative(-0.94f, 2.06f); lineTo(17f, 15.5f); lineToRelative(2.06f, 0.94f); lineTo(20f, 18.5f); lineToRelative(0.94f, -2.06f); lineTo(23f, 15.5f); lineToRelative(-2.06f, -0.94f); lineTo(20f, 12.5f); close()
                moveTo(17.71f, 9.12f); lineToRelative(-2.83f, -2.83f); curveToRelative(-0.39f, -0.39f, -1.02f, -0.39f, -1.41f, 0f); lineTo(1.29f, 18.58f); curveToRelative(-0.39f, 0.39f, -0.39f, 1.02f, 0f, 1.41f); lineToRelative(2.83f, 2.83f); curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f); lineToRelative(12.18f, -12.18f); curveToRelative(0.39f, -0.39f, 0.39f, -1.03f, 0f, -1.42f); close()
                moveTo(14.18f, 9.83f); lineToRelative(-1.41f, -1.41f); lineToRelative(1.41f, -1.41f); lineToRelative(1.41f, 1.41f); lineToRelative(-1.41f, 1.41f); close()
            }
        }

        val People: ImageVector = buildIcon("Filled.People") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 11f); curveToRelative(1.66f, 0f, 2.99f, -1.34f, 2.99f, -3f); reflectiveCurveTo(17.66f, 5f, 16f, 5f); curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f); reflectiveCurveToRelative(1.34f, 3f, 3f, 3f); close()
                moveTo(8f, 11f); curveToRelative(1.66f, 0f, 2.99f, -1.34f, 2.99f, -3f); reflectiveCurveTo(9.66f, 5f, 8f, 5f); curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f); reflectiveCurveToRelative(1.34f, 3f, 3f, 3f); close()
                moveTo(8f, 13f); curveToRelative(-2.33f, 0f, -7f, 1.17f, -7f, 3.5f); verticalLineTo(19f); horizontalLineToRelative(14f); verticalLineToRelative(-2.5f); curveToRelative(0f, -2.33f, -4.67f, -3.5f, -7f, -3.5f); close()
                moveTo(16f, 13f); curveToRelative(-0.29f, 0f, -0.62f, 0.02f, -0.97f, 0.05f); curveToRelative(1.16f, 0.84f, 1.97f, 1.97f, 1.97f, 3.45f); verticalLineTo(19f); horizontalLineToRelative(6f); verticalLineToRelative(-2.5f); curveToRelative(0f, -2.33f, -4.67f, -3.5f, -7f, -3.5f); close()
            }
        }

        val Face: ImageVector = buildIcon("Filled.Face") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); reflectiveCurveToRelative(4.48f, 10f, 10f, 10f); reflectiveCurveToRelative(10f, -4.48f, 10f, -10f); reflectiveCurveTo(17.52f, 2f, 12f, 2f); close()
                moveTo(12f, 20f); curveToRelative(-4.41f, 0f, -8f, -3.59f, -8f, -8f); curveToRelative(0f, -0.29f, 0.02f, -0.58f, 0.05f, -0.86f); curveToRelative(2.36f, -1.05f, 4.42f, -2.85f, 5.74f, -5.1f); curveToRelative(1.76f, 2.81f, 4.46f, 4.87f, 7.66f, 5.62f); curveToRelative(0f, 4.41f, -3.59f, 8f, -8f, 8f); close()
                moveTo(9f, 11.75f); curveToRelative(-0.41f, 0f, -0.75f, 0.34f, -0.75f, 0.75f); reflectiveCurveToRelative(0.34f, 0.75f, 0.75f, 0.75f); reflectiveCurveToRelative(0.75f, -0.34f, 0.75f, -0.75f); reflectiveCurveToRelative(-0.34f, -0.75f, -0.75f, -0.75f); close()
                moveTo(15f, 11.75f); curveToRelative(-0.41f, 0f, -0.75f, 0.34f, -0.75f, 0.75f); reflectiveCurveToRelative(0.34f, 0.75f, 0.75f, 0.75f); reflectiveCurveToRelative(0.75f, -0.34f, 0.75f, -0.75f); reflectiveCurveToRelative(-0.34f, -0.75f, -0.75f, -0.75f); close()
            }
        }

        val HealthAndSafety: ImageVector = buildIcon("Filled.HealthAndSafety") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); lineTo(4f, 5f); verticalLineToRelative(6.09f); curveToRelative(0f, 5.05f, 3.41f, 9.76f, 8f, 10.91f); curveToRelative(4.59f, -1.15f, 8f, -5.86f, 8f, -10.91f); verticalLineTo(5f); lineToRelative(-8f, -3f); close()
                moveTo(18f, 11.09f); curveToRelative(0f, 4f, -2.55f, 7.7f, -6f, 8.83f); curveToRelative(-3.45f, -1.13f, -6f, -4.82f, -6f, -8.83f); verticalLineTo(6.31f); lineToRelative(6f, -2.25f); lineToRelative(6f, 2.25f); verticalLineToRelative(4.78f); close()
                moveTo(10.5f, 13f); horizontalLineTo(8f); verticalLineToRelative(-3f); horizontalLineToRelative(2.5f); verticalLineTo(7.5f); horizontalLineToRelative(3f); verticalLineTo(10f); horizontalLineTo(16f); verticalLineToRelative(3f); horizontalLineToRelative(-2.5f); verticalLineToRelative(2.5f); horizontalLineToRelative(-3f); verticalLineTo(13f); close()
            }
        }

        val SportsMartialArts: ImageVector = buildIcon("Filled.SportsMartialArts") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.8f, 2f); lineToRelative(-8.2f, 6.7f); lineToRelative(-1.21f, -1.04f); lineToRelative(3.6f, -2.08f); lineTo(13.6f, 4f); lineTo(8.1f, 7.3f); curveToRelative(-0.1f, 0.1f, -0.2f, 0.1f, -0.3f, 0.2f); curveToRelative(-0.5f, 0.6f, -0.5f, 1.5f, 0f, 2f); lineToRelative(5.1f, 4.3f); verticalLineTo(22f); horizontalLineToRelative(2f); verticalLineToRelative(-6.7f); lineToRelative(-3.5f, -3f); lineToRelative(2.8f, -5.6f); lineTo(19.8f, 2f); close()
                moveTo(5f, 9f); curveToRelative(-0.6f, 0f, -1.1f, 0.4f, -1.3f, 1f); lineTo(2f, 16f); horizontalLineToRelative(2f); lineToRelative(1.1f, -4.4f); lineToRelative(4.1f, 4.1f); lineToRelative(1.4f, -1.4f); lineTo(5f, 9f); close()
                moveTo(11f, 5f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
            }
        }

        val Explore: ImageVector = buildIcon("Filled.Explore") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); reflectiveCurveToRelative(4.48f, 10f, 10f, 10f); reflectiveCurveToRelative(10f, -4.48f, 10f, -10f); reflectiveCurveTo(17.52f, 2f, 12f, 2f); close()
                moveTo(12f, 20f); curveToRelative(-4.41f, 0f, -8f, -3.59f, -8f, -8f); reflectiveCurveToRelative(3.59f, -8f, 8f, -8f); reflectiveCurveToRelative(8f, 3.59f, 8f, 8f); reflectiveCurveToRelative(-3.59f, 8f, -8f, 8f); close()
                moveTo(14.1f, 14.1f); lineTo(18f, 6f); lineToRelative(-8.1f, 3.9f); lineTo(6f, 14.1f); lineToRelative(8.1f, -3.9f); lineToRelative(0f, 8f); close()
            }
        }

        val Bolt: ImageVector = buildIcon("Filled.Bolt") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(11f, 21f); horizontalLineToRelative(-1f); lineToRelative(1f, -7f); horizontalLineTo(7f); lineToRelative(6f, -13f); horizontalLineToRelative(1f); lineToRelative(-1f, 7f); horizontalLineToRelative(4f); lineTo(11f, 21f); close()
            }
        }

        val Shield: ImageVector = buildIcon("Filled.Shield") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 1f); lineTo(3f, 5f); verticalLineToRelative(6f); curveToRelative(0f, 5.55f, 3.84f, 10.74f, 9f, 12f); curveToRelative(5.16f, -1.26f, 9f, -6.45f, 9f, -12f); verticalLineTo(5f); lineTo(12f, 1f); close()
            }
        }

        val Description: ImageVector = buildIcon("Filled.Description") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(14f, 2f); horizontalLineTo(6f); curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f); lineTo(4f, 20f); curveToRelative(0f, 1.1f, 0.89f, 2f, 1.99f, 2f); horizontalLineTo(18f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(8f); lineToRelative(-6f, -6f); close()
                moveTo(16f, 18f); horizontalLineTo(8f); verticalLineToRelative(-2f); horizontalLineTo(16f); verticalLineToRelative(2f); close()
                moveTo(16f, 14f); horizontalLineTo(8f); verticalLineToRelative(-2f); horizontalLineTo(16f); verticalLineToRelative(2f); close()
                moveTo(13f, 9f); verticalLineTo(3.5f); lineTo(18.5f, 9f); horizontalLineTo(13f); close()
            }
        }

        val PhotoCamera: ImageVector = buildIcon("Filled.PhotoCamera") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9f, 2f); lineTo(7.17f, 4f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(16f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(6f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); horizontalLineToRelative(-3.17f); lineTo(15f, 2f); horizontalLineTo(9f); close()
                moveTo(12f, 17f); curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f); reflectiveCurveToRelative(2.24f, -5f, 5f, -5f); reflectiveCurveToRelative(5f, 2.24f, 5f, 5f); reflectiveCurveToRelative(-2.24f, 5f, -5f, 5f); close()
                moveTo(12f, 9f); curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f); reflectiveCurveToRelative(1.34f, 3f, 3f, 3f); reflectiveCurveToRelative(3f, -1.34f, 3f, -3f); reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f); close()
            }
        }

        val Notes: ImageVector = buildIcon("Filled.Notes") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 11.01f); lineTo(3f, 11.01f); verticalLineToRelative(2f); horizontalLineToRelative(18f); verticalLineToRelative(-2f); close()
                moveTo(3f, 16.01f); lineTo(15f, 16.01f); verticalLineToRelative(2f); horizontalLineToRelative(-12f); verticalLineToRelative(-2f); close()
                moveTo(21f, 6.01f); lineTo(3f, 6.01f); verticalLineToRelative(2f); horizontalLineToRelative(18f); verticalLineToRelative(-2f); close()
            }
        }

        val LocationOn: ImageVector = buildIcon("Filled.LocationOn") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveToRelative(-3.87f, 0f, -7f, 3.13f, -7f, 7f); curveToRelative(0f, 5.25f, 7f, 13f, 7f, 13f); reflectiveCurveToRelative(7f, -7.75f, 7f, -13f); curveToRelative(0f, -3.87f, -3.13f, -7f, -7f, -7f); close()
                moveTo(12f, 11.5f); curveToRelative(-1.38f, 0f, -2.5f, -1.12f, -2.5f, -2.5f); reflectiveCurveToRelative(1.12f, -2.5f, 2.5f, -2.5f); reflectiveCurveToRelative(2.5f, 1.12f, 2.5f, 2.5f); reflectiveCurveToRelative(-1.12f, 2.5f, -2.5f, 2.5f); close()
            }
        }

        val History: ImageVector = buildIcon("Filled.History") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13f, 3f); curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f); horizontalLineTo(1f); lineToRelative(3.89f, 3.89f); lineToRelative(0.07f, 0.14f); lineTo(9f, 12f); horizontalLineTo(6f); curveToRelative(0f, -3.87f, 3.13f, -7f, 7f, -7f); reflectiveCurveToRelative(7f, 3.13f, 7f, 7f); reflectiveCurveToRelative(-3.13f, 7f, -7f, 7f); curveToRelative(-1.93f, 0f, -3.68f, -0.79f, -4.94f, -2.06f); lineToRelative(-1.42f, 1.42f); curveTo(8.27f, 19.99f, 10.51f, 21f, 13f, 21f); curveToRelative(4.97f, 0f, 9f, -4.03f, 9f, -9f); reflectiveCurveToRelative(-4.03f, -9f, -9f, -9f); close()
                moveTo(12f, 8f); verticalLineToRelative(5f); lineToRelative(4.28f, 2.54f); lineToRelative(0.72f, -1.21f); lineToRelative(-3.5f, -2.08f); verticalLineTo(8f); horizontalLineTo(12f); close()
            }
        }

        val FitnessCenter: ImageVector = buildIcon("Filled.FitnessCenter") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.57f, 14.86f); lineToRelative(1.43f, -1.43f); lineToRelative(-1.43f, -1.43f); lineToRelative(-3.57f, 3.57f); lineToRelative(-8.57f, -8.57f); lineTo(12f, 3.43f); lineToRelative(-1.43f, -1.43f); lineToRelative(-1.43f, 1.43f); lineToRelative(-1.43f, -1.43f); lineToRelative(-2.14f, 2.14f); lineToRelative(-1.43f, -1.43f); lineToRelative(-1.43f, 1.43f); lineToRelative(1.43f, 1.43f); lineTo(2f, 7.71f); lineToRelative(1.43f, 1.43f); lineTo(2f, 10.57f); lineToRelative(1.43f, 1.43f); lineToRelative(3.57f, -3.57f); lineToRelative(8.57f, 8.57f); lineToRelative(-3.57f, 3.57f); lineToRelative(1.43f, 1.43f); lineToRelative(1.43f, -1.43f); lineToRelative(1.43f, 1.43f); lineToRelative(2.14f, -2.14f); lineToRelative(1.43f, 1.43f); lineToRelative(1.43f, -1.43f); lineToRelative(-1.43f, -1.43f); lineToRelative(1.43f, -1.43f); close()
            }
        }

        val Psychology: ImageVector = buildIcon("Filled.Psychology") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); curveToRelative(0f, 4.42f, 2.87f, 8.17f, 6.84f, 9.39f); lineToRelative(0.4f, 0.12f); curveToRelative(0.43f, 0.14f, 0.88f, 0.22f, 1.34f, 0.23f); horizontalLineToRelative(0.01f); curveToRelative(0.44f, 0f, 0.88f, -0.07f, 1.3f, -0.2f); lineToRelative(0.41f, -0.13f); curveTo(16.19f, 20.25f, 19f, 16.48f, 19f, 12f); curveToRelative(0f, -5.52f, -4.48f, -10f, -7f, -10f); close()
                moveTo(13f, 19f); horizontalLineToRelative(-2f); verticalLineToRelative(-2f); horizontalLineTo(2f); verticalLineToRelative(2f); close()
                moveTo(13f, 15f); horizontalLineToRelative(-2f); verticalLineTo(9f); horizontalLineToRelative(2f); verticalLineToRelative(6f); close()
            }
        }

        val Visibility: ImageVector = buildIcon("Filled.Visibility") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4.5f); curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f); curveToRelative(1.73f, 4.39f, 6f, 7.5f, 11f, 7.5f); reflectiveCurveToRelative(9.27f, -3.11f, 11f, -7.5f); curveToRelative(-1.73f, -4.39f, -6f, -7.5f, -11f, -7.5f); close()
                moveTo(12f, 17f); curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f); reflectiveCurveToRelative(2.24f, -5f, 5f, -5f); reflectiveCurveToRelative(5f, 2.24f, 5f, 5f); reflectiveCurveToRelative(-2.24f, 5f, -5f, 5f); close()
                moveTo(12f, 9f); curveToRelative(-1.66f, 0f, -3f, 1.34f, -3f, 3f); reflectiveCurveToRelative(1.34f, 3f, 3f, 3f); reflectiveCurveToRelative(3f, -1.34f, 3f, -3f); reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f); close()
            }
        }

        val VisibilityOff: ImageVector = buildIcon("Filled.VisibilityOff") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 7f); curveToRelative(2.76f, 0f, 5f, 2.24f, 5f, 5f); curveToRelative(0f, 0.65f, -0.13f, 1.26f, -0.36f, 1.83f); lineToRelative(2.92f, 2.93f); curveToRelative(1.51f, -1.39f, 2.7f, -3.18f, 3.44f, -5.26f); curveToRelative(-1.73f, -4.39f, -6f, -7.5f, -11f, -7.5f); curveToRelative(-1.4f, 0f, -2.74f, 0.25f, -3.98f, 0.7f); lineToRelative(2.16f, 2.16f); curveToRelative(0.42f, -0.17f, 0.88f, -0.26f, 1.42f, -0.26f); close()
                moveTo(2f, 4.27f); lineToRelative(2.28f, 2.28f); lineToRelative(0.46f, 0.46f); curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1f, 12f); curveToRelative(1.73f, 4.39f, 6f, 7.5f, 11f, 7.5f); curveToRelative(1.55f, 0f, 2.97f, -0.3f, 4.28f, -0.82f); lineToRelative(0.42f, 0.42f); lineToRelative(3.03f, 3.03f); lineToRelative(1.27f, -1.27f); lineTo(3.27f, 3f); lineTo(2f, 4.27f); close()
                moveTo(12f, 17f); curveToRelative(-2.76f, 0f, -5f, -2.24f, -5f, -5f); curveToRelative(0f, -0.65f, 0.13f, -1.26f, 0.36f, -1.83f); lineToRelative(4.47f, 4.47f); curveToRelative(-0.42f, 0.23f, -0.88f, 0.36f, -1.83f, 0.36f); close()
            }
        }

        val Groups: ImageVector = buildIcon("Filled.Groups") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16.24f, 13.65f); curveToRelative(-1.17f, -0.52f, -2.61f, -0.9f, -4.24f, -0.9f); curveToRelative(-1.63f, 0f, -3.07f, 0.39f, -4.24f, 0.9f); curveTo(6.68f, 14.13f, 6f, 15.21f, 6f, 16.39f); verticalLineTo(18f); horizontalLineToRelative(12f); verticalLineToRelative(-1.61f); curveToRelative(0f, -1.18f, -0.68f, -2.26f, -1.76f, -2.74f); close()
                moveTo(12f, 12f); curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f); reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f); reflectiveCurveToRelative(-3f, 1.34f, -3f, 3f); reflectiveCurveToRelative(1.34f, 3f, 3f, 3f); close()
                moveTo(1.24f, 13.65f); curveTo(0.46f, 13.99f, 0f, 14.71f, 0f, 15.53f); verticalLineTo(18f); horizontalLineToRelative(4f); verticalLineToRelative(-1.61f); curveToRelative(0f, -0.83f, 0.23f, -1.61f, 0.63f, -2.28f); curveToRelative(-1.15f, -0.45f, -2.42f, -0.9f, -3.39f, -0.46f); close()
                moveTo(4.5f, 12f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(22.76f, 13.65f); curveToRelative(-0.97f, -0.44f, -2.24f, 0.01f, -3.39f, 0.46f); curveToRelative(0.4f, 0.67f, 0.63f, 1.45f, 0.63f, 2.28f); verticalLineTo(18f); horizontalLineToRelative(4f); verticalLineToRelative(-2.47f); curveToRelative(0f, -0.82f, -0.46f, -1.54f, -1.24f, -1.88f); close()
                moveTo(19.5f, 12f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
            }
        }

        val DirectionsRun: ImageVector = buildIcon("Filled.DirectionsRun") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.49f, 5.48f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(9.89f, 19.38f); lineToRelative(1f, -2.5f); lineToRelative(2.13f, 1.93f); verticalLineTo(24f); horizontalLineToRelative(2f); verticalLineToRelative(-6.04f); lineToRelative(-2.12f, -2.12f); lineToRelative(-1.37f, -2.95f); lineToRelative(2.39f, -2.31f); curveToRelative(1.28f, 1.45f, 3.09f, 2.45f, 5.08f, 2.65f); verticalLineToRelative(-2f); curveToRelative(-1.5f, -0.16f, -2.85f, -0.98f, -3.79f, -2.12f); lineTo(13.78f, 7.3f); curveToRelative(-0.32f, -0.42f, -0.81f, -0.7f, -1.36f, -0.71f); curveToRelative(-0.52f, -0.01f, -1.04f, 0.22f, -1.42f, 0.61f); lineTo(6.72f, 11.2f); curveToRelative(-0.39f, 0.41f, -0.39f, 1.08f, 0.01f, 1.49f); curveToRelative(0.4f, 0.4f, 1.05f, 0.39f, 1.44f, -0.02f); lineToRelative(3.35f, -3.32f); verticalLineToRelative(11.77f); lineToRelative(-4.14f, -1.85f); lineToRelative(-0.88f, 1.83f); lineToRelative(4.89f, 2.27f); close()
            }
        }

        val DirectionsWalk: ImageVector = buildIcon("Filled.DirectionsWalk") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.5f, 5.5f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(9.8f, 8.9f); lineTo(7f, 23f); horizontalLineToRelative(2.1f); lineToRelative(1.8f, -8f); lineToRelative(2.1f, 2f); verticalLineToRelative(6f); horizontalLineToRelative(2f); verticalLineToRelative(-7.5f); lineToRelative(-2.1f, -2f); lineToRelative(0.6f, -3f); curveToRelative(1.3f, 1.5f, 3.3f, 2.5f, 5.5f, 2.5f); verticalLineToRelative(-2f); curveToRelative(-1.9f, 0f, -3.5f, -1f, -4.3f, -2.4f); lineToRelative(-1f, -1.6f); curveToRelative(-0.4f, -0.6f, -1f, -1f, -1.7f, -1f); curveToRelative(-0.3f, 0f, -0.5f, 0.1f, -0.8f, 0.1f); lineTo(6f, 8.3f); verticalLineTo(13f); horizontalLineToRelative(2f); verticalLineTo(9.6f); lineToRelative(1.8f, -0.7f); close()
            }
        }

        val DirectionsBike: ImageVector = buildIcon("Filled.DirectionsBike") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15.5f, 5.5f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(5f, 20f); curveToRelative(-2.21f, 0f, -4f, -1.79f, -4f, -4f); reflectiveCurveToRelative(1.79f, -4f, 4f, -4f); reflectiveCurveToRelative(4f, 1.79f, 4f, 4f); reflectiveCurveToRelative(-1.79f, 4f, -4f, 4f); close()
                moveTo(5f, 13.5f); curveToRelative(-1.38f, 0f, -2.5f, 1.12f, -2.5f, 2.5f); reflectiveCurveToRelative(1.12f, 2.5f, 2.5f, 2.5f); reflectiveCurveToRelative(2.5f, -1.12f, 2.5f, -2.5f); reflectiveCurveToRelative(-1.12f, -2.5f, -2.5f, -2.5f); close()
                moveTo(19f, 20f); curveToRelative(-2.21f, 0f, -4f, -1.79f, -4f, -4f); reflectiveCurveToRelative(1.79f, -4f, 4f, -4f); reflectiveCurveToRelative(4f, 1.79f, 4f, 4f); reflectiveCurveToRelative(-1.79f, 4f, -4f, 4f); close()
                moveTo(19f, 13.5f); curveToRelative(-1.38f, 0f, -2.5f, 1.12f, -2.5f, 2.5f); reflectiveCurveToRelative(1.12f, 2.5f, 2.5f, 2.5f); reflectiveCurveToRelative(2.5f, -1.12f, 2.5f, -2.5f); reflectiveCurveToRelative(-1.12f, -2.5f, -2.5f, -2.5f); close()
                moveTo(11f, 13.5f); lineToRelative(2f, -2f); lineToRelative(2.1f, 2.1f); curveToRelative(1.11f, 1.11f, 2.65f, 1.8f, 4.35f, 1.8f); verticalLineToRelative(-2f); curveToRelative(-1.14f, 0f, -2.17f, -0.46f, -2.92f, -1.21f); lineTo(14.24f, 9.9f); curveToRelative(-0.41f, -0.41f, -1f, -0.65f, -1.6f, -0.6f); lineTo(9f, 9.9f); verticalLineTo(15f); horizontalLineToRelative(2f); verticalLineToRelative(-1.5f); close()
            }
        }

        val Accessibility: ImageVector = buildIcon("Filled.Accessibility") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f); reflectiveCurveToRelative(-0.9f, 2f, -2f, 2f); reflectiveCurveToRelative(-2f, -0.9f, -2f, -2f); reflectiveCurveToRelative(0.9f, -2f, 2f, -2f); close()
                moveTo(21f, 9f); horizontalLineToRelative(-6f); verticalLineToRelative(13f); horizontalLineToRelative(-2f); verticalLineToRelative(-6f); horizontalLineToRelative(-2f); verticalLineToRelative(6f); horizontalLineTo(9f); verticalLineTo(9f); horizontalLineTo(3f); verticalLineTo(7f); horizontalLineToRelative(18f); verticalLineToRelative(2f); close()
            }
        }

        val Lock: ImageVector = buildIcon("Filled.Lock") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 8f); horizontalLineToRelative(-1f); verticalLineTo(6f); curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f); reflectiveCurveTo(7f, 3.24f, 7f, 6f); verticalLineToRelative(2f); horizontalLineTo(6f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(10f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(12f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(10f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(9f, 6f); curveToRelative(0f, -1.66f, 1.34f, -3f, 3f, -3f); reflectiveCurveToRelative(3f, 1.34f, 3f, 3f); verticalLineToRelative(2f); horizontalLineTo(9f); verticalLineTo(6f); close()
                moveTo(12f, 17f); curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f); reflectiveCurveToRelative(0.9f, -2f, 2f, -2f); reflectiveCurveToRelative(2f, 0.9f, 2f, 2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, 2f); close()
            }
        }

        val LockOpen: ImageVector = buildIcon("Filled.LockOpen") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 17f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(18f, 8f); horizontalLineToRelative(-1f); verticalLineTo(6f); curveToRelative(0f, -2.76f, -2.24f, -5f, -5f, -5f); reflectiveCurveTo(7f, 3.24f, 7f, 6f); horizontalLineToRelative(2f); curveToRelative(0f, -1.66f, 1.34f, -3f, 3f, -3f); reflectiveCurveToRelative(3f, 1.34f, 3f, 3f); verticalLineToRelative(2f); horizontalLineTo(6f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(10f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(12f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(10f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(18f, 20f); horizontalLineTo(6f); verticalLineTo(10f); horizontalLineToRelative(12f); verticalLineToRelative(10f); close()
            }
        }

        val Casino: ImageVector = buildIcon("Filled.Casino") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 3f); horizontalLineTo(5f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(14f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(14f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(5f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(7.5f, 18f); curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f); reflectiveCurveTo(6.67f, 15f, 7.5f, 15f); reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f); reflectiveCurveTo(8.33f, 18f, 7.5f, 18f); close()
                moveTo(7.5f, 9f); curveTo(6.67f, 9f, 6f, 8.33f, 6f, 7.5f); reflectiveCurveTo(6.67f, 6f, 7.5f, 6f); reflectiveCurveTo(9f, 6.67f, 9f, 7.5f); reflectiveCurveTo(8.33f, 9f, 7.5f, 9f); close()
                moveTo(12f, 13.5f); curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f); reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f); reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f); reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f); close()
                moveTo(16.5f, 18f); curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f); reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f); reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f); reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f); close()
                moveTo(16.5f, 9f); curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f); reflectiveCurveTo(15.67f, 6f, 16.5f, 6f); reflectiveCurveTo(18f, 6.67f, 18f, 7.5f); reflectiveCurveTo(17.33f, 9f, 16.5f, 9f); close()
            }
        }

        val Mood: ImageVector = buildIcon("Filled.Mood") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(11.99f, 2f); curveTo(6.47f, 2f, 2f, 6.48f, 2f, 12f); reflectiveCurveToRelative(4.47f, 10f, 9.99f, 10f); curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f); reflectiveCurveTo(17.52f, 2f, 11.99f, 2f); close()
                moveTo(12f, 20f); curveToRelative(-4.42f, 0f, -8f, -3.58f, -8f, -8f); reflectiveCurveToRelative(3.58f, -8f, 8f, -8f); reflectiveCurveToRelative(8f, 3.58f, 8f, 8f); reflectiveCurveToRelative(-3.58f, 8f, -8f, 8f); close()
                moveTo(15.5f, 11f); curveToRelative(0.83f, 0f, 1.5f, -0.67f, 1.5f, -1.5f); reflectiveCurveTo(16.33f, 8f, 15.5f, 8f); reflectiveCurveTo(14f, 8.67f, 14f, 9.5f); reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f); close()
                moveTo(8.5f, 11f); curveToRelative(0.83f, 0f, 1.5f, -0.67f, 1.5f, -1.5f); reflectiveCurveTo(9.33f, 8f, 8.5f, 8f); reflectiveCurveTo(7f, 8.67f, 7f, 9.5f); reflectiveCurveToRelative(0.67f, 1.5f, 1.5f, 1.5f); close()
                moveTo(12f, 17.5f); curveToRelative(2.03f, 0f, 3.8f, -1.11f, 4.75f, -2.75f); lineToRelative(-1.75f, -1.01f); curveToRelative(-0.63f, 0.66f, -1.73f, 1.26f, -3f, 1.26f); reflectiveCurveToRelative(-2.37f, -0.6f, -3.01f, -1.26f); lineToRelative(-1.75f, 1.01f); curveToRelative(0.95f, 1.64f, 2.72f, 2.75f, 4.76f, 2.75f); close()
            }
        }

        val School: ImageVector = buildIcon("Filled.School") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(5f, 13.18f); verticalLineToRelative(2.81f); lineTo(12f, 20f); lineToRelative(7f, -4.01f); verticalLineToRelative(-2.81f); lineTo(12f, 17f); lineTo(5f, 13.18f); close()
                moveTo(12f, 3f); lineTo(1f, 9f); lineToRelative(11f, 6f); lineToRelative(9f, -4.91f); verticalLineTo(17f); horizontalLineToRelative(2f); verticalLineTo(9f); lineTo(12f, 3f); close()
            }
        }

        val SelfImprovement: ImageVector = buildIcon("Filled.SelfImprovement") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 16f); verticalLineToRelative(-2f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); horizontalLineTo(21f); close()
                moveTo(5f, 16f); horizontalLineTo(3f); curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f); verticalLineTo(16f); close()
                moveTo(12f, 11f); curveToRelative(1.66f, 0f, 3f, -1.34f, 3f, -3f); reflectiveCurveToRelative(-1.34f, -3f, -3f, -3f); reflectiveCurveToRelative(-3f, 1.34f, -3f, 3f); reflectiveCurveToRelative(1.34f, 3f, 3f, 3f); close()
                moveTo(12f, 13f); curveToRelative(-2.33f, 0f, -7f, 1.17f, -7f, 3.5f); verticalLineTo(19f); horizontalLineToRelative(14f); verticalLineToRelative(-2.5f); curveTo(19f, 14.17f, 14.33f, 13f, 12f, 13f); close()
                moveTo(12f, 5f); curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f); reflectiveCurveToRelative(-0.45f, -1f, -1f, -1f); reflectiveCurveToRelative(-1f, 0.45f, -1f, 1f); reflectiveCurveToRelative(0.45f, 1f, 1f, 1f); close()
            }
        }

        val HeartBroken: ImageVector = buildIcon("Filled.HeartBroken") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16.5f, 3f); curveToRelative(-1.74f, 0f, -3.41f, 0.81f, -4.5f, 2.09f); curveTo(10.91f, 3.81f, 9.24f, 3f, 7.5f, 3f); curveTo(4.42f, 3f, 2f, 5.42f, 2f, 8.5f); curveToRelative(0f, 3.78f, 3.4f, 6.86f, 8.55f, 11.54f); lineTo(12f, 21.35f); lineToRelative(1.45f, -1.32f); curveTo(18.6f, 15.36f, 22f, 12.28f, 22f, 8.5f); curveTo(22f, 5.42f, 19.58f, 3f, 16.5f, 3f); close()
            }
        }

        val Lightbulb: ImageVector = buildIcon("Filled.Lightbulb") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveTo(8.13f, 2f, 5f, 5.13f, 5f, 9f); curveToRelative(0f, 2.38f, 1.19f, 4.47f, 3f, 5.74f); verticalLineTo(17f); curveToRelative(0f, 0.55f, 0.45f, 1f, 1f, 1f); horizontalLineToRelative(6f); curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f); verticalLineToRelative(-2.26f); curveToRelative(1.81f, -1.27f, 3f, -3.36f, 3f, -5.74f); curveToRelative(0f, -3.87f, -3.13f, -7f, -7f, -7f); close()
                moveTo(13f, 20f); horizontalLineToRelative(-2f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); reflectiveCurveToRelative(2f, -0.9f, 2f, -2f); horizontalLineToRelative(-2f); close()
            }
        }

        val Dangerous: ImageVector = buildIcon("Filled.Dangerous") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveTo(6.47f, 2f, 2f, 6.47f, 2f, 12f); reflectiveCurveToRelative(4.47f, 10f, 10f, 10f); reflectiveCurveToRelative(10f, -4.47f, 10f, -10f); reflectiveCurveTo(17.53f, 2f, 12f, 2f); close()
                moveTo(17f, 15.59f); lineTo(15.59f, 17f); lineTo(12f, 13.41f); lineTo(8.41f, 17f); lineTo(7f, 15.59f); lineTo(10.59f, 12f); lineTo(7f, 8.41f); lineTo(8.41f, 7f); lineTo(12f, 10.59f); lineTo(15.59f, 7f); lineTo(17f, 8.41f); lineTo(13.41f, 12f); lineTo(17f, 15.59f); close()
            }
        }

        val LibraryBooks: ImageVector = buildIcon("Filled.LibraryBooks") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(4f, 6f); horizontalLineTo(2f); verticalLineToRelative(14f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(14f); verticalLineToRelative(-2f); horizontalLineTo(4f); verticalLineTo(6f); close()
                moveTo(20f, 2f); horizontalLineTo(8f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(12f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(12f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(4f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(20f, 16f); horizontalLineTo(8f); verticalLineTo(4f); horizontalLineToRelative(12f); verticalLineToRelative(12f); close()
                moveTo(18f, 11f); horizontalLineToRelative(-8f); verticalLineToRelative(2f); horizontalLineToRelative(8f); verticalLineToRelative(-2f); close()
                moveTo(18f, 7f); horizontalLineToRelative(-8f); verticalLineToRelative(2f); horizontalLineToRelative(8f); verticalLineTo(7f); close()
                moveTo(14f, 13f); horizontalLineToRelative(-4f); verticalLineToRelative(2f); horizontalLineToRelative(4f); verticalLineToRelative(-2f); close()
            }
        }

        val MenuBook: ImageVector = buildIcon("Filled.MenuBook") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 5f); curveToRelative(-1.11f, -0.35f, -2.33f, -0.5f, -3.5f, -0.5f); curveToRelative(-1.95f, 0f, -4.05f, 0.4f, -5.5f, 1.5f); curveToRelative(-1.45f, -1.1f, -3.55f, -1.5f, -5.5f, -1.5f); curveToRelative(-1.17f, 0f, -2.39f, 0.15f, -3.5f, 0.5f); curveToRelative(-0.75f, 0.25f, -1.5f, 0.81f, -1.5f, 1.67f); verticalLineToRelative(14.26f); curveToRelative(0f, 0.12f, 0.01f, 0.24f, 0.04f, 0.36f); curveToRelative(0.22f, 0.81f, 1.09f, 1.21f, 1.86f, 0.97f); curveToRelative(1.03f, -0.32f, 2.11f, -0.46f, 3.1f, -0.46f); curveToRelative(2f, 0f, 4.25f, 0.45f, 5.5f, 1.5f); curveToRelative(1.25f, -1.05f, 3.5f, -1.5f, 5.5f, -1.5f); curveToRelative(0.99f, 0f, 2.07f, 0.14f, 3.1f, 0.46f); curveToRelative(0.77f, 0.24f, 1.64f, -0.16f, 1.86f, -0.97f); curveToRelative(0.03f, -0.12f, 0.04f, -0.24f, 0.04f, -0.36f); verticalLineTo(6.67f); curveToRelative(0f, -0.86f, -0.75f, -1.42f, -1.5f, -1.67f); close()
                moveTo(21f, 18.5f); curveToRelative(-1.1f, -0.35f, -2.3f, -0.5f, -3.5f, -0.5f); curveToRelative(-1.7f, 0f, -3.45f, 0.3f, -5f, 1f); verticalLineTo(7.5f); curveToRelative(1.55f, -0.7f, 3.3f, -1f, 5f, -1f); curveToRelative(1.2f, 0f, 2.4f, 0.15f, 3.5f, 0.5f); verticalLineToRelative(11.5f); close()
            }
        }

        val Build: ImageVector = buildIcon("Filled.Build") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(22.7f, 19f); lineToRelative(-9.12f, -9.1f); curveToRelative(0.4f, -1.28f, 0.14f, -2.77f, -0.88f, -3.79f); curveToRelative(-1.16f, -1.16f, -2.8f, -1.5f, -4.26f, -1.02f); lineToRelative(2.8f, 2.8f); lineToRelative(-2.83f, 2.83f); lineToRelative(-2.8f, -2.8f); curveToRelative(-0.49f, 1.46f, -0.15f, 3.1f, 1.01f, 4.26f); curveToRelative(1.01f, 1.01f, 2.5f, 1.27f, 3.77f, 0.88f); lineToRelative(9.12f, 9.1f); curveToRelative(0.39f, 0.39f, 1.02f, 0.39f, 1.41f, 0f); lineToRelative(0.79f, -0.79f); curveToRelative(0.39f, -0.39f, 0.39f, -1.02f, 0f, -1.41f); close()
            }
        }

        val Pets: ImageVector = buildIcon("Filled.Pets") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 11f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(13f, 6f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(7f, 6f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(5f, 11f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); reflectiveCurveToRelative(-0.9f, -2f, -2f, -2f); reflectiveCurveToRelative(-2f, 0.9f, -2f, 2f); reflectiveCurveToRelative(0.9f, 2f, 2f, 2f); close()
                moveTo(12f, 13f); curveToRelative(-1.97f, 0f, -5f, 0.81f, -5f, 3f); curveToRelative(0f, 1.25f, 1.13f, 2.14f, 2.45f, 2.71f); curveToRelative(0.85f, 0.36f, 1.54f, 1.17f, 1.95f, 2.06f); curveToRelative(0.1f, 0.23f, 1.1f, 0.23f, 1.2f, 0f); curveToRelative(0.41f, -0.89f, 1.1f, -1.7f, 1.95f, -2.06f); curveToRelative(1.32f, -0.57f, 2.45f, -1.46f, 2.45f, -2.71f); curveToRelative(0f, -2.19f, -3.03f, -3f, -5f, -3f); close()
            }
        }

        val PersonAdd: ImageVector = buildIcon("Filled.PersonAdd") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15f, 12f); curveToRelative(2.21f, 0f, 4f, -1.79f, 4f, -4f); reflectiveCurveToRelative(-1.79f, -4f, -4f, -4f); reflectiveCurveToRelative(-4f, 1.79f, -4f, 4f); reflectiveCurveToRelative(1.79f, 4f, 4f, 4f); close()
                moveTo(6f, 10f); verticalLineTo(7f); horizontalLineTo(4f); verticalLineToRelative(3f); horizontalLineTo(1f); verticalLineToRelative(2f); horizontalLineToRelative(3f); verticalLineToRelative(3f); horizontalLineToRelative(2f); verticalLineToRelative(-3f); horizontalLineToRelative(3f); verticalLineToRelative(-2f); horizontalLineTo(6f); close()
                moveTo(15f, 14f); curveToRelative(-2.67f, 0f, -8f, 1.34f, -8f, 4f); verticalLineToRelative(2f); horizontalLineToRelative(16f); verticalLineToRelative(-2f); curveToRelative(0f, -2.66f, -5.33f, -4f, -8f, -4f); close()
            }
        }

        val ContentCopy: ImageVector = buildIcon("Filled.ContentCopy") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 1f); horizontalLineTo(4f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(14f); horizontalLineToRelative(2f); verticalLineTo(3f); horizontalLineToRelative(12f); verticalLineTo(1f); close()
                moveTo(19f, 5f); horizontalLineTo(8f); curveToRelative(-1.13f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(14f); curveToRelative(0f, 1.1f, 0.87f, 2f, 2f, 2f); horizontalLineToRelative(11f); curveToRelative(1.13f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(7f); curveToRelative(0f, -1.1f, -0.87f, -2f, -2f, -2f); close()
                moveTo(19f, 21f); horizontalLineTo(8f); verticalLineTo(7f); horizontalLineToRelative(11f); verticalLineToRelative(14f); close()
            }
        }

        val ContentPaste: ImageVector = buildIcon("Filled.ContentPaste") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 2f); horizontalLineToRelative(-4.18f); curveTo(14.4f, 0.84f, 13.3f, 0f, 12f, 0f); curveToRelative(-1.3f, 0f, -2.4f, 0.84f, -2.82f, 2f); horizontalLineTo(5f); curveToRelative(-1.1f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(16f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(14f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(4f); curveToRelative(0f, -1.1f, -0.9f, -2f, -2f, -2f); close()
                moveTo(12f, 2f); curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f); reflectiveCurveToRelative(-0.45f, 1f, -1f, 1f); reflectiveCurveToRelative(-1f, -0.45f, -1f, -1f); reflectiveCurveToRelative(0.45f, -1f, 1f, -1f); close()
                moveTo(19f, 20f); horizontalLineTo(5f); verticalLineTo(4f); horizontalLineToRelative(2f); verticalLineToRelative(3f); horizontalLineToRelative(10f); verticalLineTo(4f); horizontalLineToRelative(2f); verticalLineToRelative(16f); close()
            }
        }

        val Palette: ImageVector = buildIcon("Filled.Palette") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 3f); curveToRelative(-4.97f, 0f, -9f, 4.03f, -9f, 9f); reflectiveCurveToRelative(4.03f, 9f, 9f, 9f); curveToRelative(0.83f, 0f, 1.5f, -0.67f, 1.5f, -1.5f); curveToRelative(0f, -0.39f, -0.15f, -0.75f, -0.41f, -1.01f); curveToRelative(-0.25f, -0.26f, -0.41f, -0.61f, -0.41f, -0.99f); curveToRelative(0f, -0.83f, 0.67f, -1.5f, 1.5f, -1.5f); horizontalLineTo(16f); curveToRelative(2.76f, 0f, 5f, -2.24f, 5f, -5f); curveToRelative(0f, -4.42f, -4.03f, -8f, -9f, -8f); close()
                moveTo(6.5f, 12f); curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f); reflectiveCurveTo(5.67f, 9f, 6.5f, 9f); reflectiveCurveTo(8f, 9.67f, 8f, 10.5f); reflectiveCurveTo(7.33f, 12f, 6.5f, 12f); close()
                moveTo(9.5f, 8f); curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f); reflectiveCurveTo(8.67f, 5f, 9.5f, 5f); reflectiveCurveTo(11f, 5.67f, 11f, 6.5f); reflectiveCurveTo(10.33f, 8f, 9.5f, 8f); close()
                moveTo(14.5f, 8f); curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f); reflectiveCurveTo(13.67f, 5f, 14.5f, 5f); reflectiveCurveTo(16f, 5.67f, 16f, 6.5f); reflectiveCurveTo(15.33f, 8f, 14.5f, 8f); close()
                moveTo(17.5f, 12f); curveToRelative(-0.83f, 0f, -1.5f, -0.67f, -1.5f, -1.5f); reflectiveCurveTo(16.67f, 9f, 17.5f, 9f); reflectiveCurveTo(19f, 9.67f, 19f, 10.5f); reflectiveCurveTo(18.33f, 12f, 17.5f, 12f); close()
            }
        }

        val Storage: ImageVector = buildIcon("Filled.Storage") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(2f, 20f); horizontalLineToRelative(20f); verticalLineToRelative(-4f); horizontalLineTo(2f); verticalLineToRelative(4f); close()
                moveTo(4f, 17f); horizontalLineToRelative(2f); verticalLineToRelative(2f); horizontalLineTo(4f); verticalLineToRelative(-2f); close()
                moveTo(2f, 4f); verticalLineToRelative(4f); horizontalLineToRelative(20f); verticalLineTo(4f); horizontalLineTo(2f); close()
                moveTo(6f, 7f); horizontalLineTo(4f); verticalLineTo(5f); horizontalLineToRelative(2f); verticalLineToRelative(2f); close()
                moveTo(2f, 14f); horizontalLineToRelative(20f); verticalLineToRelative(-4f); horizontalLineTo(2f); verticalLineToRelative(4f); close()
                moveTo(4f, 11f); horizontalLineToRelative(2f); verticalLineToRelative(2f); horizontalLineTo(4f); verticalLineToRelative(-2f); close()
            }
        }

        val UploadFile: ImageVector = buildIcon("Filled.UploadFile") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(14f, 2f); horizontalLineTo(6f); curveToRelative(-1.1f, 0f, -1.99f, 0.9f, -1.99f, 2f); lineTo(4f, 20f); curveToRelative(0f, 1.1f, 0.89f, 2f, 1.99f, 2f); horizontalLineTo(18f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(8f); lineToRelative(-6f, -6f); close()
                moveTo(13f, 9f); verticalLineTo(3.5f); lineTo(18.5f, 9f); horizontalLineTo(13f); close()
                moveTo(12f, 18f); lineToRelative(-4f, -4f); horizontalLineToRelative(3f); verticalLineToRelative(-4f); horizontalLineToRelative(2f); verticalLineTo(14f); horizontalLineToRelative(3f); lineToRelative(-4f, 4f); close()
            }
        }

        val ExpandMore: ImageVector = buildIcon("Filled.ExpandMore") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16.59f, 8.59f); lineTo(12f, 13.17f); lineTo(7.41f, 8.59f); lineTo(6f, 10f); lineToRelative(6f, 6f); lineToRelative(6f, -6f); lineToRelative(-1.41f, -1.41f); close()
            }
        }

        val ExpandLess: ImageVector = buildIcon("Filled.ExpandLess") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 8f); lineToRelative(-6f, 6f); lineToRelative(1.41f, 1.41f); lineTo(12f, 10.83f); lineToRelative(4.59f, 4.58f); lineTo(18f, 14f); lineToRelative(-6f, -6f); close()
            }
        }

        val FavoriteBorder: ImageVector = buildIcon("Filled.FavoriteBorder") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16.5f, 3f); curveToRelative(-1.74f, 0f, -3.41f, 0.81f, -4.5f, 2.09f); curveTo(10.91f, 3.81f, 9.24f, 3f, 7.5f, 3f); curveTo(4.42f, 3f, 2f, 5.42f, 2f, 8.5f); curveToRelative(0f, 3.78f, 3.4f, 6.86f, 8.55f, 11.54f); lineTo(12f, 21.35f); lineToRelative(1.45f, -1.32f); curveTo(18.6f, 15.36f, 22f, 12.28f, 22f, 8.5f); curveTo(22f, 5.42f, 19.58f, 3f, 16.5f, 3f); close()
                moveTo(12.1f, 18.55f); lineToRelative(-0.1f, 0.1f); lineToRelative(-0.1f, -0.1f); curveTo(7.14f, 14.24f, 4f, 11.39f, 4f, 8.5f); curveToRelative(0f, -1.5f, 1f, -2.5f, 2.5f, -2.5f); curveToRelative(1.13f, 0f, 2.26f, 0.74f, 2.65f, 1.74f); horizontalLineToRelative(1.7f); curveToRelative(0.39f, -1f, 1.52f, -1.74f, 2.65f, -1.74f); curveToRelative(1.5f, 0f, 2.5f, 1f, 2.5f, 2.5f); curveToRelative(0f, 2.89f, -3.14f, 5.74f, -7.9f, 10.05f); close()
            }
        }

        val SouthEast: ImageVector = buildIcon("Filled.SouthEast") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 9f); horizontalLineToRelative(-2f); verticalLineToRelative(6.59f); lineTo(5.41f, 4f); lineTo(4f, 5.41f); lineTo(15.59f, 17f); horizontalLineTo(9f); verticalLineToRelative(2f); horizontalLineToRelative(10f); verticalLineTo(9f); close()
            }
        }

        val Save: ImageVector = buildIcon("Filled.Save") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17f, 3f); horizontalLineTo(5f); curveToRelative(-1.11f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(14f); curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f); horizontalLineToRelative(14f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(7f); lineToRelative(-4f, -4f); close()
                moveTo(12f, 19f); curveToRelative(-1.66f, 0f, -3f, -1.34f, -3f, -3f); reflectiveCurveToRelative(1.34f, -3f, 3f, -3f); reflectiveCurveToRelative(3f, 1.34f, 3f, 3f); reflectiveCurveToRelative(-1.34f, 3f, -3f, 3f); close()
                moveTo(15f, 9f); horizontalLineTo(5f); verticalLineTo(5f); horizontalLineToRelative(10f); verticalLineToRelative(4f); close()
            }
        }

        val AddCircle: ImageVector = buildIcon("Filled.AddCircle") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f); curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f); reflectiveCurveToRelative(4.48f, 10f, 10f, 10f); reflectiveCurveToRelative(10f, -4.48f, 10f, -10f); reflectiveCurveTo(17.52f, 2f, 12f, 2f); close()
                moveTo(17f, 13f); horizontalLineToRelative(-4f); verticalLineToRelative(4f); horizontalLineToRelative(-2f); verticalLineToRelative(-4f); horizontalLineTo(7f); verticalLineToRelative(-2f); horizontalLineToRelative(4f); verticalLineTo(7f); horizontalLineToRelative(2f); verticalLineToRelative(4f); horizontalLineToRelative(4f); verticalLineToRelative(2f); close()
            }
        }

        val DeleteSweep: ImageVector = buildIcon("Filled.DeleteSweep") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(15f, 16f); horizontalLineToRelative(4f); verticalLineToRelative(2f); horizontalLineToRelative(-4f); verticalLineToRelative(-2f); close()
                moveTo(15f, 8f); horizontalLineToRelative(7f); verticalLineToRelative(2f); horizontalLineToRelative(-7f); verticalLineTo(8f); close()
                moveTo(15f, 12f); horizontalLineToRelative(6f); verticalLineToRelative(2f); horizontalLineToRelative(-6f); verticalLineToRelative(-2f); close()
                moveTo(3f, 18f); curveToRelative(0f, 1.1f, 0.9f, 2f, 2f, 2f); horizontalLineToRelative(6f); curveToRelative(1.1f, 0f, 2f, -0.9f, 2f, -2f); verticalLineTo(8f); horizontalLineTo(3f); verticalLineToRelative(10f); close()
                moveTo(14f, 5f); horizontalLineToRelative(-3f); lineToRelative(-1f, -1f); horizontalLineTo(6f); lineToRelative(-1f, 1f); horizontalLineTo(2f); verticalLineToRelative(2f); horizontalLineToRelative(12f); verticalLineTo(5f); close()
            }
        }

        val Stop: ImageVector = buildIcon("Filled.Stop") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6f, 6f); horizontalLineToRelative(12f); verticalLineToRelative(12f); horizontalLineTo(6f); close()
            }
        }
    }
}
