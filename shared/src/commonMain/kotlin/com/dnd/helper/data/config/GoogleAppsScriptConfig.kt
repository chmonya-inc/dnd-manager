package com.dnd.helper.data.config

import com.dnd.helper.di.isDesktop

/**
 * Google Apps Script Web App URL.
 *
 * The actual URLs are injected at build time from **local.properties**
 * (keys: `apps.script.url.android` and `apps.script.url.desktop`) into [GeneratedConfig].
 */
object GoogleAppsScriptConfig {
    val WEB_APP_URL = if (isDesktop) {
        GeneratedConfig.WEB_APP_URL_DESKTOP
    } else {
        GeneratedConfig.WEB_APP_URL_ANDROID
    }
}
