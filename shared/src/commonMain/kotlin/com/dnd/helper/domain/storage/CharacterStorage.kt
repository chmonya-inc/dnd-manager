package com.dnd.helper.domain.storage

interface CharacterStorage {
    fun saveCharacterId(id: String)
    fun getCharacterId(): String?
}
