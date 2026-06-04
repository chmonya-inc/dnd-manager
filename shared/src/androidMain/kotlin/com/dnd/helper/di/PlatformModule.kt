package com.dnd.helper.di

import android.content.Context
import com.dnd.helper.domain.music.AndroidAudioPlayer
import com.dnd.helper.domain.music.AudioPlayer
import com.dnd.helper.domain.storage.CharacterStorage
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class AndroidCharacterStorage(context: Context) : CharacterStorage {
    private val prefs = context.getSharedPreferences("dnd_helper_prefs", Context.MODE_PRIVATE)
    
    override fun saveCharacterId(id: String) {
        prefs.edit().putString("last_character_id", id).apply()
    }

    override fun getCharacterId(): String? {
        return prefs.getString("last_character_id", null)
    }

    override fun saveTableId(id: String) {
        prefs.edit().putString("last_table_id", id).apply()
    }

    override fun getTableId(): String? {
        return prefs.getString("last_table_id", null)
    }

    override fun saveSessions(sessionsJson: String) {
        prefs.edit().putString("saved_sessions", sessionsJson).apply()
    }

    override fun getSessions(): String? {
        return prefs.getString("saved_sessions", null)
    }

    override fun saveTheme(themeName: String) {
        prefs.edit().putString("app_theme", themeName).apply()
    }

    override fun getTheme(): String? {
        return prefs.getString("app_theme", null)
    }

    override fun saveComfyUiAddress(address: String) {
        prefs.edit().putString("comfy_ui_address", address).apply()
    }

    override fun getComfyUiAddress(): String? {
        return prefs.getString("comfy_ui_address", null)
    }

    override fun saveComfyUiWorkflow(json: String) {
        prefs.edit().putString("comfy_ui_workflow", json).apply()
    }

    override fun getComfyUiWorkflow(): String? {
        return prefs.getString("comfy_ui_workflow", null)
    }

    override fun saveGenerationSteps(steps: Int) {
        prefs.edit().putInt("gen_steps", steps).apply()
    }

    override fun getGenerationSteps(): Int {
        return prefs.getInt("gen_steps", 20)
    }

    override fun saveApiCache(key: String, json: String) {
        // Android implementation placeholder
    }

    override fun getApiCache(key: String): String? {
        return null
    }

    override fun clearApiCache() {
        // Android implementation placeholder
    }
}

actual val platformModule = module {
    single<CharacterStorage> { AndroidCharacterStorage(androidContext()) }
    single<AudioPlayer> { AndroidAudioPlayer() }
}

actual val isDesktop: Boolean = false
actual val isWeb: Boolean = false

actual fun openUrl(url: String) {
    // Android implementation...
}

actual fun pickFile(title: String, allowedExtensions: List<String>): String? {
    return null // Not implemented for Android yet
}

actual fun readFileContent(path: String): String? {
    return null
}
