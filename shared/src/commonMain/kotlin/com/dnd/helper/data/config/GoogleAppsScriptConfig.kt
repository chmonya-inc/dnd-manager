package com.dnd.helper.data.config

import com.dnd.helper.di.isDesktop
import com.dnd.helper.di.isWeb

/**
 * Google Apps Script Web App URL.
 *
 * The actual URLs are injected at build time from **local.properties**
 * (keys: `apps.script.url.android` and `apps.script.url.desktop`) into [GeneratedConfig].
 */
object GoogleAppsScriptConfig {
    val WEB_APP_URL = when {
        isDesktop -> GeneratedConfig.WEB_APP_URL_DESKTOP
        isWeb -> GeneratedConfig.WEB_APP_URL_DESKTOP // Web also uses localhost
        else -> GeneratedConfig.WEB_APP_URL_ANDROID
    }
}
