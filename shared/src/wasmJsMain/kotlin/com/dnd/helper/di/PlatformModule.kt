package com.dnd.helper.di

import com.dnd.helper.domain.storage.CharacterStorage
import org.koin.dsl.module
import kotlinx.browser.localStorage

class WasmCharacterStorage : CharacterStorage {
    override fun saveCharacterId(id: String) {
        localStorage.setItem("last_character_id", id)
    }

    override fun getCharacterId(): String? {
        return localStorage.getItem("last_character_id")
    }

    override fun saveTableId(id: String) {
        localStorage.setItem("last_table_id", id)
    }

    override fun getTableId(): String? {
        return localStorage.getItem("last_table_id")
    }

    override fun saveSessions(sessionsJson: String) {
        localStorage.setItem("saved_sessions", sessionsJson)
    }

    override fun getSessions(): String? {
        return localStorage.getItem("saved_sessions")
    }

    override fun saveTheme(themeName: String) {
        localStorage.setItem("app_theme", themeName)
    }

    override fun getTheme(): String? {
        return localStorage.getItem("app_theme")
    }
}

actual val platformModule = module {
    single<CharacterStorage> { WasmCharacterStorage() }
}

actual val isDesktop: Boolean = false

actual fun openUrl(url: String) {
    kotlinx.browser.window.open(url, "_blank")
}
