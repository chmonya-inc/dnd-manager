package com.dnd.helper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.dnd.helper.di.coreModule
import com.dnd.helper.di.platformModule
import com.dnd.helper.theme.DndHelperTheme
import com.dnd.helper.theme.ThemeViewModel
import io.ktor.client.HttpClient
import org.koin.compose.KoinApplication
import org.koin.compose.getKoin
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

@Composable
fun CoreApp(
    koinConfiguration: KoinAppDeclaration = {},
    appModules: List<Module> = emptyList(),
    content: @Composable () -> Unit
) {
    KoinApplication(application = {
        koinConfiguration()
        modules(listOf(coreModule, platformModule) + appModules)
    }) {
        val koin = getKoin()
        val httpClient = remember { koin.get<HttpClient>() }
        remember(httpClient) {
            SingletonImageLoader.setSafe { context ->
                ImageLoader.Builder(context)
                    .components {
                        add(KtorNetworkFetcherFactory(httpClient))
                    }
                    .crossfade(true)
                    .logger(DebugLogger())
                    .build()
            }
        }

        val themeViewModel: ThemeViewModel = koinViewModel()
        val currentTheme by themeViewModel.currentTheme.collectAsState()

        DndHelperTheme(theme = currentTheme) {
            content()
        }
    }
}
