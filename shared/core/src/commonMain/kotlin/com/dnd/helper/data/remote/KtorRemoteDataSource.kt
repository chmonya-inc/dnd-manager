package com.dnd.helper.data.remote

import com.dnd.helper.data.config.GoogleAppsScriptConfig
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.*
import com.dnd.helper.domain.storage.CharacterStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class KtorRemoteDataSource(
    private val httpClient: HttpClient,
    private val storage: CharacterStorage,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    private fun sessionId(): String = storage.getTableId() ?: "default"
    
    private fun baseUrl(address: String? = storage.getServerAddress()): String {
        if (address != null && address.isNotBlank()) {
            var formatted = address.trim()
            if (!formatted.startsWith("http://") && !formatted.startsWith("https://")) {
                formatted = "http://$formatted"
            }
            return formatted.removeSuffix("/exec").removeSuffix("/")
        }
        return GoogleAppsScriptConfig.WEB_APP_URL.removeSuffix("/exec").removeSuffix("/")
    }

    private fun wsUrl(address: String? = storage.getServerAddress()): String {
        return baseUrl(address).replace("http://", "ws://").replace("https://", "wss://")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeUpdates(): Flow<String> = kotlinx.coroutines.flow.combine(
        storage.getServerAddressFlow(),
        storage.getTableIdFlow()
    ) { address, tableId ->
        address to tableId
    }.flatMapLatest { (address, tableId) ->
        flow {
            while (true) {
                try {
                    val currentTableId = tableId ?: "default"
                    val url = "${wsUrl(address)}/api/$currentTableId/ws"
                    println("[KtorRemoteDataSource] Connecting to WebSocket: $url")
                    httpClient.webSocket(url) {
                        // Send a "check" message every 1 second to trigger update notifications
                        val pinger = launch {
                            while (isActive) {
                                try {
                                    send("check")
                                    delay(1000)
                                } catch (e: Exception) {
                                    break
                                }
                            }
                        }

                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                if (text.startsWith("update:")) {
                                    emit(text.removePrefix("update:"))
                                }
                            }
                        }
                        pinger.cancel()
                    }
                } catch (e: Exception) {
                    println("[KtorRemoteDataSource] WS Error: ${e.message}")
                }
                delay(5000) // Reconnect delay
            }
        }
    }

    suspend fun getInitialData(): Result<InitialData> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/initial-data") }

    suspend fun getCharacters(): Result<List<Character>> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/characters") }

    suspend fun getCharacter(id: String): Result<Character> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/characters/$id") }

    suspend fun saveCharacter(character: Character): Result<Unit> =
        safeApiCall { 
            httpClient.post("${baseUrl()}/api/${sessionId()}/characters") {
                contentType(ContentType.Application.Json)
                setBody(character)
            }
        }

    suspend fun deleteCharacter(id: String): Result<Unit> =
        safeApiCall { httpClient.delete("${baseUrl()}/api/${sessionId()}/characters/$id") }

    suspend fun getLocations(): Result<List<Location>> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/locations") }

    suspend fun saveLocation(location: Location): Result<Unit> =
        safeApiCall {
            httpClient.post("${baseUrl()}/api/${sessionId()}/locations") {
                contentType(ContentType.Application.Json)
                setBody(location)
            }
        }

    suspend fun deleteLocation(id: String): Result<Unit> =
        safeApiCall { httpClient.delete("${baseUrl()}/api/${sessionId()}/locations/$id") }

    suspend fun getBattlefields(): Result<List<Battlefield>> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/battlefields") }

    suspend fun saveBattlefield(battlefield: Battlefield): Result<Unit> =
        safeApiCall {
            httpClient.post("${baseUrl()}/api/${sessionId()}/battlefields") {
                contentType(ContentType.Application.Json)
                setBody(battlefield)
            }
        }

    suspend fun deleteBattlefield(id: String): Result<Unit> =
        safeApiCall { httpClient.delete("${baseUrl()}/api/${sessionId()}/battlefields/$id") }

    suspend fun getMonsters(): Result<List<Monster>> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/monsters") }

    suspend fun saveMonster(monster: Monster): Result<Unit> =
        safeApiCall {
            httpClient.post("${baseUrl()}/api/${sessionId()}/monsters") {
                contentType(ContentType.Application.Json)
                setBody(monster)
            }
        }

    suspend fun deleteMonster(id: String): Result<Unit> =
        safeApiCall { httpClient.delete("${baseUrl()}/api/${sessionId()}/monsters/$id") }

    suspend fun getNpcs(): Result<List<Npc>> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/npcs") }

    suspend fun saveNpc(npc: Npc): Result<Unit> =
        safeApiCall {
            httpClient.post("${baseUrl()}/api/${sessionId()}/npcs") {
                contentType(ContentType.Application.Json)
                setBody(npc)
            }
        }

    suspend fun deleteNpc(id: String): Result<Unit> =
        safeApiCall { httpClient.delete("${baseUrl()}/api/${sessionId()}/npcs/$id") }

    suspend fun getMusic(): Result<List<MusicTrack>> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/music") }

    suspend fun saveMusic(music: MusicTrack): Result<Unit> =
        safeApiCall {
            httpClient.post("${baseUrl()}/api/${sessionId()}/music") {
                contentType(ContentType.Application.Json)
                setBody(music)
            }
        }

    suspend fun deleteMusic(id: String): Result<Unit> =
        safeApiCall { httpClient.delete("${baseUrl()}/api/${sessionId()}/music/$id") }

    suspend fun getLogs(): Result<List<LogEntry>> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/logs") }

    suspend fun saveLog(log: LogEntry): Result<Unit> =
        safeApiCall {
            httpClient.post("${baseUrl()}/api/${sessionId()}/logs") {
                contentType(ContentType.Application.Json)
                setBody(log)
            }
        }

    suspend fun getEvents(): Result<List<GameEvent>> =
        safeApiCall { httpClient.get("${baseUrl()}/api/${sessionId()}/events") }

    suspend fun saveEvent(event: GameEvent): Result<Unit> =
        safeApiCall {
            httpClient.post("${baseUrl()}/api/${sessionId()}/events") {
                contentType(ContentType.Application.Json)
                setBody(event)
            }
        }

    suspend fun deleteEvent(id: String): Result<Unit> =
        safeApiCall { httpClient.delete("${baseUrl()}/api/${sessionId()}/events/$id") }

    private suspend inline fun <reified T> safeApiCall(
        call: () -> io.ktor.client.statement.HttpResponse
    ): Result<T> {
        return try {
            val response = call()
            if (response.status.isSuccess()) {
                if (T::class == Unit::class) {
                    Result.Success(Unit as T)
                } else {
                    Result.Success(response.body<T>())
                }
            } else {
                Result.Error(AppError.Unknown("Server returned ${response.status}"))
            }
        } catch (e: Exception) {
            println("[KtorRemoteDataSource] Error: ${e.message}")
            Result.Error(AppError.Unknown(e.message ?: "Unknown error"))
        }
    }
}
