package com.dnd.helper

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(
        title = "D&D Helper",
        canvasElementId = "ComposeTarget",
    ) {
        App()
    }
}
