package com.dnd.helper.data.config

/**
 * Google Apps Script Web App URL.
 *
 * The actual URL is injected at build time from **local.properties**
 * (key: `apps.script.url`) into [GeneratedConfig].
 *
 * To set / change the URL:
 * 1. Open `local.properties` in the project root
 * 2. Add or edit: `apps.script.url=https://script.google.com/macros/s/YOUR_ID/exec`
 * 3. Sync Gradle
 *
 * `local.properties` is already in `.gitignore` and will never be committed.
 */
object GoogleAppsScriptConfig {
    const val WEB_APP_URL = GeneratedConfig.WEB_APP_URL
}
