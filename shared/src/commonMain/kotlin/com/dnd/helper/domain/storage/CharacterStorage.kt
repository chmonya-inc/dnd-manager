package com.dnd.helper.domain.storage

interface CharacterStorage {
    fun saveCharacterId(id: String)
    fun getCharacterId(): String?
    fun saveTableId(id: String)
    fun getTableId(): String?
    fun saveSessions(sessionsJson: String)
    fun getSessions(): String?
}
