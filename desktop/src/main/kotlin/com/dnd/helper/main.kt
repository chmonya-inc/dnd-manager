package com.dnd.helper

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.dnd.helper.theme.DndHelperTheme

fun main() = application {
    val windowState = rememberWindowState(placement = WindowPlacement.Fullscreen)

    Window(
        onCloseRequest = ::exitApplication,
        title = "D&D Helper",
        state = windowState
    ) {
        DndHelperTheme {
            App()
        }
    }
}
