package com.dnd.helper

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.dnd.helper.App
import com.dnd.helper.theme.DndHelperTheme

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(
        title = "D&D Helper",
        canvasElementId = "ComposeTarget",
    ) {
        DndHelperTheme {
            App()
        }
    }
}
