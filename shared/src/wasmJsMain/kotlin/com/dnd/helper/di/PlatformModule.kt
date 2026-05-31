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
}

actual val platformModule = module {
    single<CharacterStorage> { WasmCharacterStorage() }
}

actual val isDesktop: Boolean = false
