package com.dnd.helper.di

import android.content.Context
import com.dnd.helper.domain.music.AndroidAudioPlayer
import com.dnd.helper.domain.music.AudioPlayer
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.serialization.json.*
import kotlinx.serialization.encodeToString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class AndroidCharacterStorage(context: Context) : CharacterStorage {
    private val prefs = context.getSharedPreferences("dnd_helper_prefs", Context.MODE_PRIVATE)
    private val _serverAddressFlow = MutableStateFlow(getServerAddress())
    private val _tableIdFlow = MutableStateFlow(getTableId())
    private val _userIdFlow = MutableStateFlow(getUserId())

    override fun saveCharacterId(id: String) {
        prefs.edit().putString("last_character_id", id).apply()
    }

    override fun getCharacterId(): String? {
        return prefs.getString("last_character_id", null)
    }

    override fun saveTableId(id: String) {
        prefs.edit().putString("last_table_id", id).apply()
        _tableIdFlow.value = id
    }

    override fun getTableId(): String? {
        return prefs.getString("last_table_id", null)
    }

    override fun getTableIdFlow(): kotlinx.coroutines.flow.Flow<String?> {
        return _tableIdFlow.asStateFlow()
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

    override fun saveServerAddress(address: String) {
        prefs.edit().putString("main_server_address", address).apply()
        _serverAddressFlow.value = address
    }

    override fun getServerAddress(): String? {
        return prefs.getString("main_server_address", null)
    }

    override fun getServerAddressFlow(): kotlinx.coroutines.flow.Flow<String?> {
        return _serverAddressFlow.asStateFlow()
    }

    override fun saveComfyUiAddress(address: String) {
        prefs.edit().putString("comfy_ui_address", address).apply()
    }

    override fun getComfyUiAddress(): String? {
        return prefs.getString("comfy_ui_address", null)
    }

    override fun saveComfyUi(workflow: JsonObject) {
        prefs.edit().putString("comfy_ui_workflow", Json.encodeToString(workflow)).apply()
    }

    override fun getComfyUiWorkflow(): JsonObject? {
        val jsonStr = prefs.getString("comfy_ui_workflow", null) ?: return null
        return try {
            Json.decodeFromString<JsonObject>(jsonStr)
        } catch (e: Exception) {
            null
        }
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

    override fun saveAuthToken(token: String?) {
        prefs.edit().putString("auth_token", token).apply()
    }

    override fun getAuthToken(): String? {
        return prefs.getString("auth_token", null)
    }

    override fun saveRefreshToken(token: String?) {
        prefs.edit().putString("refresh_token", token).apply()
    }

    override fun getRefreshToken(): String? {
        return prefs.getString("refresh_token", null)
    }

    override fun saveUserId(userId: String?) {
        prefs.edit().putString("user_id", userId).apply()
        _userIdFlow.value = userId
    }

    override fun getUserId(): String? {
        return prefs.getString("user_id", null)
    }

    override fun getUserIdFlow(): kotlinx.coroutines.flow.Flow<String?> {
        return _userIdFlow.asStateFlow()
    }

    override fun saveUserRole(role: String?) {
        prefs.edit().putString("user_role", role).apply()
    }

    override fun getUserRole(): String? {
        return prefs.getString("user_role", null)
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

actual suspend fun pasteFromClipboard(): String? {
    return try {
        val context = org.koin.core.context.GlobalContext.get().get<Context>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            item?.text?.toString()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
