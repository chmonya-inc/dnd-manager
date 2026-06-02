package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState

@Composable
actual fun AppWebView(url: String, modifier: Modifier) {
    val state = rememberWebViewState(url)
    WebView(
        state = state,
        modifier = modifier
    )
}
