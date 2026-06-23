package com.dnd.helper.di

import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import org.koin.dsl.module
import kotlinx.browser.localStorage
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class WasmCharacterStorage : CharacterStorage {
    private val _serverAddressFlow = MutableStateFlow(getServerAddress())
    private val _tableIdFlow = MutableStateFlow(getTableId())

    override fun saveCharacterId(id: String) {
        localStorage.setItem("last_character_id", id)
    }

    override fun getCharacterId(): String? {
        return localStorage.getItem("last_character_id")
    }

    override fun saveTableId(id: String) {
        localStorage.setItem("last_table_id", id)
        _tableIdFlow.value = id
    }

    override fun getTableId(): String? {
        return localStorage.getItem("last_table_id")
    }

    override fun getTableIdFlow(): kotlinx.coroutines.flow.Flow<String?> {
        return _tableIdFlow.asStateFlow()
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

    override fun saveServerAddress(address: String) {
        localStorage.setItem("main_server_address", address)
        _serverAddressFlow.value = address
    }

    override fun getServerAddress(): String? {
        return localStorage.getItem("main_server_address")
    }

    override fun getServerAddressFlow(): kotlinx.coroutines.flow.Flow<String?> {
        return _serverAddressFlow.asStateFlow()
    }

    override fun saveComfyUiAddress(address: String) {
        localStorage.setItem("comfy_ui_address", address)
    }

    override fun getComfyUiAddress(): String? {
        return localStorage.getItem("comfy_ui_address")
    }

    override fun saveComfyUi(workflow: JsonObject) {
        localStorage.setItem("comfy_ui_workflow", Json.encodeToString(workflow))
    }

    override fun getComfyUiWorkflow(): JsonObject? {
        val jsonStr = localStorage.getItem("comfy_ui_workflow") ?: return null
        return try {
            Json.decodeFromString<JsonObject>(jsonStr)
        } catch (e: Exception) {
            null
        }
    }

    override fun saveGenerationSteps(steps: Int) {
        localStorage.setItem("gen_steps", steps.toString())
    }

    override fun getGenerationSteps(): Int {
        return localStorage.getItem("gen_steps")?.toIntOrNull() ?: 20
    }

    override fun saveApiCache(key: String, json: String) {
        // Wasm implementation placeholder
    }

    override fun getApiCache(key: String): String? {
        return null
    }

    override fun clearApiCache() {
        // Wasm implementation placeholder
    }
}

actual val platformModule = module {
    single<CharacterStorage> { WasmCharacterStorage() }
}

actual val isDesktop: Boolean = false
actual val isWeb: Boolean = true

actual fun openUrl(url: String) {
    kotlinx.browser.window.open(url, "_blank")
}

actual fun pickFile(title: String, allowedExtensions: List<String>): String? {
    return null
}

actual fun readFileContent(path: String): String? {
    return null
}

actual suspend fun pasteFromClipboard(): String? {
    return try {
        val clipboard = kotlinx.browser.window.navigator.clipboard
        val promise = clipboard.readText()
        val text = promise.toArisPromise<JsString>().await<JsString>()
        text.toString()
    } catch (e: Exception) {
        null
    }
}

private fun <T : JsAny> JsAny.toArisPromise(): kotlin.js.Promise<T> = this.unsafeCast()
