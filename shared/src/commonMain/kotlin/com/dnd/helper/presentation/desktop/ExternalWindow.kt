package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.Composable

@Composable
expect fun ExternalWindow(
    isOpen: Boolean,
    onCloseRequest: () -> Unit,
    title: String = "D&D Helper - Player View",
    content: @Composable () -> Unit
)
