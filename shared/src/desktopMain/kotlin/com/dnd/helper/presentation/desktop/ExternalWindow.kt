package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window

@Composable
actual fun ExternalWindow(
    isOpen: Boolean,
    onCloseRequest: () -> Unit,
    title: String,
    content: @Composable () -> Unit
) {
    if (isOpen) {
        Window(
            onCloseRequest = onCloseRequest,
            title = title
        ) {
            content()
        }
    }
}
