package com.dnd.helper.fakes

import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.JsonObject

/**
 * In-memory CharacterStorage for repository tests. The flows are real [MutableStateFlow]s so
 * that repository code which combines `getTableIdFlow/getServerAddressFlow/getUserIdFlow`
 * reacts to changes made via `saveTableId/saveServerAddress/saveUserId`.
 */
class FakeCharacterStorage : CharacterStorage {

    private val _tableIdFlow = MutableStateFlow<String?>(null)
    private val _serverAddressFlow = MutableStateFlow<String?>(null)
    private val _userIdFlow = MutableStateFlow<String?>(null)

    var storedCharacterId: String? = null
    var storedTableId: String?
        get() = _tableIdFlow.value
        set(value) { _tableIdFlow.value = value }
    var storedUserId: String?
        get() = _userIdFlow.value
        set(value) { _userIdFlow.value = value }
    var storedUserRole: String? = null
    var storedAuthToken: String? = null
    var storedRefreshToken: String? = null

    override fun saveCharacterId(id: String) { storedCharacterId = id }
    override fun getCharacterId(): String? = storedCharacterId

    override fun saveTableId(id: String) { _tableIdFlow.value = id }
    override fun getTableId(): String? = _tableIdFlow.value
    override fun getTableIdFlow(): Flow<String?> = _tableIdFlow.asStateFlow()

    override fun saveSessions(sessionsJson: String) {}
    override fun getSessions(): String? = null

    override fun saveTheme(themeName: String) {}
    override fun getTheme(): String? = null

    override fun saveServerAddress(address: String) { _serverAddressFlow.value = address }
    override fun getServerAddress(): String? = _serverAddressFlow.value
    override fun getServerAddressFlow(): Flow<String?> = _serverAddressFlow.asStateFlow()

    override fun saveComfyUiAddress(address: String) {}
    override fun getComfyUiAddress(): String? = null
    override fun saveComfyUi(workflow: JsonObject) {}
    override fun getComfyUiWorkflow(): JsonObject? = null
    override fun saveGenerationSteps(steps: Int) {}
    override fun getGenerationSteps(): Int = 20
    override fun saveApiCache(key: String, json: String) {}
    override fun getApiCache(key: String): String? = null
    override fun clearApiCache() {}

    override fun saveAuthToken(token: String?) { storedAuthToken = token }
    override fun getAuthToken(): String? = storedAuthToken
    override fun saveRefreshToken(token: String?) { storedRefreshToken = token }
    override fun getRefreshToken(): String? = storedRefreshToken

    override fun saveUserId(userId: String?) { _userIdFlow.value = userId }
    override fun getUserId(): String? = _userIdFlow.value
    override fun getUserIdFlow(): Flow<String?> = _userIdFlow.asStateFlow()

    override fun saveUserRole(role: String?) { storedUserRole = role }
    override fun getUserRole(): String? = storedUserRole
}
