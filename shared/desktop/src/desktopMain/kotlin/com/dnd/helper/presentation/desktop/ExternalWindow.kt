package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import java.awt.GraphicsEnvironment

@Composable
actual fun ExternalWindow(
    isOpen: Boolean,
    onCloseRequest: () -> Unit,
    title: String,
    content: @Composable () -> Unit
) {
    if (isOpen) {
        // 1. Pre-calculate screen configuration to avoid dynamic changes that cause crashes
        val screenConfig = remember {
            try {
                val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
                val screens = ge.screenDevices
                // Use secondary screen if available, otherwise primary
                val device = if (screens.size > 1) screens[1] else screens[0]
                val config = device.defaultConfiguration
                val bounds = config.bounds

                // In AWT, coordinates are already in logical units on modern JDKs (HiDPI aware)
                // We use Floating + undecorated instead of Fullscreen to avoid the 0xC000041D crash
                Triple(
                    WindowPosition(bounds.x.dp, bounds.y.dp),
                    DpSize(bounds.width.dp, bounds.height.dp),
                    true // found target
                )
            } catch (e: Throwable) {
                Triple(WindowPosition(Alignment.Center), DpSize(800.dp, 600.dp), false)
            }
        }

        val windowState = rememberWindowState(
            position = screenConfig.first,
            size = screenConfig.second,
            placement = WindowPlacement.Floating
        )

        Window(
            onCloseRequest = onCloseRequest,
            title = title,
            state = windowState,
            undecorated = true, // Full immersion
            transparent = false,
            resizable = false,
            focusable = true
        ) {
            Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
