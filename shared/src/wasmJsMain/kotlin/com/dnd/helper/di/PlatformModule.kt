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

    override fun saveComfyUiAddress(address: String) {
        localStorage.setItem("comfy_ui_address", address)
    }

    override fun getComfyUiAddress(): String? {
        return localStorage.getItem("comfy_ui_address")
    }

    override fun saveComfyUiWorkflow(json: String) {
        localStorage.setItem("comfy_ui_workflow", json)
    }

    override fun getComfyUiWorkflow(): String? {
        return localStorage.getItem("comfy_ui_workflow")
    }

    override fun saveGenerationSteps(steps: Int) {
        localStorage.setItem("gen_steps", steps.toString())
    }

    override fun getGenerationSteps(): Int {
        return localStorage.getItem("gen_steps")?.toIntOrNull() ?: 20
    }
}

actual val platformModule = module {
    single<CharacterStorage> { WasmCharacterStorage() }
}

actual val isDesktop: Boolean = false

actual fun openUrl(url: String) {
    kotlinx.browser.window.open(url, "_blank")
}

actual fun pickFile(title: String, allowedExtensions: List<String>): String? {
    return null
}

actual fun readFileContent(path: String): String? {
    return null
}
