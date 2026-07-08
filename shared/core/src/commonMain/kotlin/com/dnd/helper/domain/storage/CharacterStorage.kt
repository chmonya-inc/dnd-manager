package com.dnd.helper.domain.storage

import kotlinx.serialization.json.JsonObject

interface CharacterStorage {
    fun saveCharacterId(id: String)
    fun getCharacterId(): String?
    fun saveTableId(id: String)
    fun getTableId(): String?
    fun getTableIdFlow(): kotlinx.coroutines.flow.Flow<String?>
    fun saveSessions(sessionsJson: String)
    fun getSessions(): String?
    fun saveTheme(themeName: String)
    fun getTheme(): String?
    fun saveServerAddress(address: String)
    fun getServerAddress(): String?
    fun getServerAddressFlow(): kotlinx.coroutines.flow.Flow<String?>
    fun saveComfyUiAddress(address: String)
    fun getComfyUiAddress(): String?
    fun saveComfyUi(workflow: JsonObject)
    fun getComfyUiWorkflow(): JsonObject?
    fun saveGenerationSteps(steps: Int)
    fun getGenerationSteps(): Int
    fun saveApiCache(key: String, json: String)
    fun getApiCache(key: String): String?
    fun clearApiCache()
    fun saveAuthToken(token: String?)
    fun getAuthToken(): String?
    fun saveRefreshToken(token: String?)
    fun getRefreshToken(): String?
}
