package com.dnd.helper.di

import com.dnd.helper.domain.storage.CharacterStorage
import org.koin.dsl.module
import java.util.prefs.Preferences

class DesktopCharacterStorage : CharacterStorage {
    private val prefs = Preferences.userRoot().node("com.dnd.helper")
    
    override fun saveCharacterId(id: String) {
        prefs.put("last_character_id", id)
    }

    override fun getCharacterId(): String? {
        return prefs.get("last_character_id", null)
    }

    override fun saveTableId(id: String) {
        prefs.put("last_table_id", id)
    }

    override fun getTableId(): String? {
        return prefs.get("last_table_id", null)
    }

    override fun saveSessions(sessionsJson: String) {
        prefs.put("saved_sessions", sessionsJson)
    }

    override fun getSessions(): String? {
        return prefs.get("saved_sessions", null)
    }
}

actual val platformModule = module {
    single<CharacterStorage> { DesktopCharacterStorage() }
}

actual val isDesktop: Boolean = true
