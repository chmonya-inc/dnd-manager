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
}

actual val platformModule = module {
    single<CharacterStorage> { AndroidCharacterStorage(androidContext()) }
    single<AudioPlayer> { AndroidAudioPlayer() }
}

actual val isDesktop: Boolean = false
