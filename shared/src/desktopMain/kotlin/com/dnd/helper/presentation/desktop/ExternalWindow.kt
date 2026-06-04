package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
        val screenConfig = remember {
            val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val screens = ge.screenDevices
            if (screens.size > 1) {
                // Secondary screen found
                val bounds = screens[1].defaultConfiguration.bounds
                Pair(
                    WindowPosition(bounds.x.dp, bounds.y.dp),
                    WindowPlacement.Fullscreen
                )
            } else {
                // Only one screen, just fullscreen
                Pair(
                    WindowPosition(Alignment.Center),
                    WindowPlacement.Fullscreen
                )
            }
        }

        val windowState = rememberWindowState(
            position = screenConfig.first,
            placement = screenConfig.second
        )

        Window(
            onCloseRequest = onCloseRequest,
            title = title,
            state = windowState,
            undecorated = true, // Hide window title bar and controls for true immersion
            focusable = false // Hint to OS to not steal focus from the fullscreen main window
        ) {
            content()
        }
    }
}
