package com.dnd.helper.fakes

import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject

class FakeCharacterStorage : CharacterStorage {
    var storedCharacterId: String? = null
    var storedTableId: String? = null
    var storedUserId: String? = null
    var storedUserRole: String? = "PLAYER"
    var storedServerAddress: String? = null
    var storedComfyUiAddress: String? = null
    var storedComfyUiWorkflow: JsonObject? = null
    var storedGenerationSteps: Int = 20

    private val _tableIdFlow = MutableStateFlow<String?>(null)
    private val _userIdFlow = MutableStateFlow<String?>(null)

    override fun saveCharacterId(id: String) { storedCharacterId = id }
    override fun getCharacterId(): String? = storedCharacterId
    override fun saveTableId(id: String) {
        storedTableId = id
        _tableIdFlow.value = id
    }
    override fun getTableId(): String? = storedTableId
    override fun getTableIdFlow(): Flow<String?> = _tableIdFlow.asStateFlow()
    override fun saveSessions(sessionsJson: String) {}
    override fun getSessions(): String? = null
    override fun saveTheme(themeName: String) {}
    override fun getTheme(): String? = null
    override fun saveServerAddress(address: String) { storedServerAddress = address }
    override fun getServerAddress(): String? = storedServerAddress
    override fun getServerAddressFlow(): Flow<String?> = MutableStateFlow(storedServerAddress)
    override fun saveComfyUiAddress(address: String) { storedComfyUiAddress = address }
    override fun getComfyUiAddress(): String? = storedComfyUiAddress
    override fun saveComfyUi(workflow: JsonObject) { storedComfyUiWorkflow = workflow }
    override fun getComfyUiWorkflow(): JsonObject? = storedComfyUiWorkflow
    override fun saveGenerationSteps(steps: Int) { storedGenerationSteps = steps }
    override fun getGenerationSteps(): Int = storedGenerationSteps
    override fun saveApiCache(key: String, json: String) {}
    override fun getApiCache(key: String): String? = null
    override fun clearApiCache() {}
    override fun saveAuthToken(token: String?) {}
    override fun getAuthToken(): String? = "fake-token"
    override fun saveRefreshToken(token: String?) {}
    override fun getRefreshToken(): String? = "fake-refresh"
    override fun saveUserId(userId: String?) {
        storedUserId = userId
        _userIdFlow.value = userId
    }
    override fun getUserId(): String? = storedUserId
    override fun getUserIdFlow(): Flow<String?> = _userIdFlow.asStateFlow()
    override fun saveUserRole(role: String?) { storedUserRole = role }
    override fun getUserRole(): String? = storedUserRole
}
