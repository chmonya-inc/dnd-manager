package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.Composable

@Composable
actual fun ExternalWindow(
    isOpen: Boolean,
    onCloseRequest: () -> Unit,
    title: String,
    content: @Composable () -> Unit
) {
    // No external windows on Android
}
