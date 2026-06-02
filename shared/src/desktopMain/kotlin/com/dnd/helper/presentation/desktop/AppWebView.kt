package com.dnd.helper.presentation.desktop

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState
import dev.datlag.kcef.KCEF
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import org.cef.network.CefCookieManager

@Composable
actual fun AppWebView(url: String, modifier: Modifier) {
    var isLoading by remember { mutableStateOf(true) }
    var engineInitialized by remember { mutableStateOf(false) }

    // Use a persistent directory for user data
    val userDataDir = remember {
        val dir = File(System.getProperty("user.home"), ".dndhelper/webview-cache")
        if (!dir.exists()) dir.mkdirs()
        dir
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                // Initialize KCEF with persistence enabled
                KCEF.init(
                    builder = {
                        installDir(File("kcef-bundle"))
                        settings {
                            cachePath = userDataDir.absolutePath
                            persistSessionCookies = true
                        }
                        // Added stability flags and explicit cookie persistence
                        addArgs("--disable-gpu", "--no-sandbox", "--persist-session-cookies")
                        progress {
                            onDownloading { isLoading = true }
                            onInitialized { 
                                engineInitialized = true
                                isLoading = false 
                            }
                        }
                    },
                    onError = { engineInitialized = false; isLoading = false },
                    onRestartRequired = { engineInitialized = false; isLoading = false }
                )
            } catch (e: Exception) {
                // Recover if already initialized or other non-fatal error
                engineInitialized = true 
                isLoading = false
            }
        }
    }

    // Flush cookies on dispose to ensure they hit the disk
    DisposableEffect(Unit) {
        onDispose {
            try {
                CefCookieManager.getGlobalManager()?.flushStore(null)
            } catch (e: Exception) {
                // Ignore dispose errors
            }
        }
    }

    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (engineInitialized) {
        val state = rememberWebViewState(url)
        state.webSettings.apply {
            isJavaScriptEnabled = true
        }
        
        WebView(
            state = state,
            modifier = modifier.fillMaxSize()
        )
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Failed to initialize WebView engine.")
        }
    }
}
