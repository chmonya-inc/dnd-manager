package com.dnd.helper.di

import android.content.Context
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
}

actual val platformModule = module {
    single<CharacterStorage> { AndroidCharacterStorage(androidContext()) }
}

actual val isDesktop: Boolean = false
