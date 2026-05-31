package com.dnd.helper

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.dnd.helper.theme.DndHelperTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "D&D Helper",
    ) {
            DndHelperTheme {
                App()
            }
    }
}
