package com.dnd.helper.presentation.desktop

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState

@Composable
actual fun AppWebView(url: String, modifier: Modifier) {
    val state = rememberWebViewState(url)
    
    // Enable cookies and data persistence for Android
    state.webSettings.apply {
        isJavaScriptEnabled = true
        androidWebSettings.apply {
            domStorageEnabled = true
            supportZoom = true
        }
    }

    WebView(
        state = state,
        modifier = modifier
    )
}
