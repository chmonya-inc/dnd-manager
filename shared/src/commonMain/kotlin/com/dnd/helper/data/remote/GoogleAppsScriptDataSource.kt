package com.dnd.helper.data.remote

import com.dnd.helper.data.config.GoogleAppsScriptConfig
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.storage.CharacterStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

class GoogleAppsScriptDataSource(
    private val httpClient: HttpClient,
    private val storage: CharacterStorage,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    private fun tableId(): String? = storage.getTableId()

    suspend fun getInitialData(): Result<com.dnd.helper.domain.model.InitialData> =
        execute(request = AppsScriptRequest(action = "getInitialData", tableId = tableId()))

    suspend fun getCharacters(): Result<List<Character>> =
        execute(request = AppsScriptRequest(action = "getCharacters", tableId = tableId()))

    suspend fun getCharacter(id: String): Result<Character> =
        execute(request = AppsScriptRequest(action = "getCharacter", id = id, tableId = tableId()))

    suspend fun saveCharacter(character: Character): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "saveCharacter", character = character, tableId = tableId()))

    suspend fun deleteCharacter(id: String): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "deleteCharacter", id = id, tableId = tableId()))

    suspend fun getLocations(): Result<List<Location>> =
        execute(request = AppsScriptRequest(action = "getLocations", tableId = tableId()))

    suspend fun saveLocation(location: Location): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "saveLocation", location = location, tableId = tableId()))

    suspend fun deleteLocation(id: String): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "deleteLocation", id = id, tableId = tableId()))

    suspend fun getMonsters(): Result<List<com.dnd.helper.domain.model.Monster>> =
        execute(request = AppsScriptRequest(action = "getMonsters", tableId = tableId()))

    suspend fun saveMonster(monster: com.dnd.helper.domain.model.Monster): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "saveMonster", monster = monster, tableId = tableId()))

    suspend fun deleteMonster(id: String): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "deleteMonster", id = id, tableId = tableId()))

    suspend fun getNpcs(): Result<List<com.dnd.helper.domain.model.Npc>> =
        execute(request = AppsScriptRequest(action = "getNpcs", tableId = tableId()))

    suspend fun saveNpc(npc: com.dnd.helper.domain.model.Npc): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "saveNpc", npc = npc, tableId = tableId()))

    suspend fun deleteNpc(id: String): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "deleteNpc", id = id, tableId = tableId()))

    suspend fun getMusic(): Result<List<com.dnd.helper.domain.model.MusicTrack>> =
        execute(request = AppsScriptRequest(action = "getMusic", tableId = tableId()))

    suspend fun saveMusic(music: com.dnd.helper.domain.model.MusicTrack): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "saveMusic", music = music, tableId = tableId()))

    suspend fun deleteMusic(id: String): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "deleteMusic", id = id, tableId = tableId()))

    suspend fun getLogs(): Result<List<com.dnd.helper.domain.model.LogEntry>> =
        execute(request = AppsScriptRequest(action = "getLogs", tableId = tableId()))

    suspend fun saveLog(log: com.dnd.helper.domain.model.LogEntry): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "saveLog", log = log, tableId = tableId()))

    /**
     * Returns the last-modified timestamp from the server.
     * This is a lightweight poll endpoint — clients call it frequently
     * and only fetch full data when the timestamp changes.
     */
    suspend fun getLastModified(): Result<String> =
        execute(request = AppsScriptRequest(action = "getLastModified", tableId = tableId()))

    /**
     * Builds the request URL with the JSON payload encoded as a query parameter.
     *
     * Google Apps Script Web Apps return HTTP 302 for POST requests, converting
     * them to GET and dropping the body. By using GET directly with the request
     * JSON in the URL, we avoid the redirect issue entirely.
     */
    private fun buildUrl(request: AppsScriptRequest): String {
        val jsonStr = json.encodeToString(request)
        val encoded = jsonStr.encodeURLQueryComponent()
        return "${GoogleAppsScriptConfig.WEB_APP_URL}?request=$encoded"
    }

    private suspend inline fun <reified T> execute(
        request: AppsScriptRequest,
    ): Result<T> {
        return try {
            val url = buildUrl(request)
            println("[AppsScript] → GET $url")

            val response = httpClient.get(url)
            val rawBody: String = response.body()

            println("[AppsScript] ← Status: ${response.status}")
            println("[AppsScript] ← Raw: ${rawBody.take(400)}")

            if (!response.status.isSuccess()) {
                return Result.Error(
                    AppError.Unknown(httpErrorHint(response.status.value, rawBody))
                )
            }

            val parsed = try {
                json.decodeFromString<AppsScriptResponse<T>>(rawBody)
            } catch (e: Exception) {
                println("[AppsScript] JSON parse error: $e")
                // Fallback for single object vs list: 
                // If we expected T (e.g. Character) but got [T], try to parse as List<T> and take first
                if (rawBody.contains("\"data\":[")) {
                    try {
                        val listParsed = json.decodeFromString<AppsScriptResponse<List<T>>>(rawBody)
                        if (listParsed.success && !listParsed.data.isNullOrEmpty()) {
                            val first = listParsed.data.first()
                            // This only works if T is what's inside the list. 
                            // Since we can't easily check T at runtime here, we just return Success(first)
                            // and let the caller handle type mismatches if any.
                            return Result.Success(first)
                        }
                    } catch (e2: Exception) {
                        println("[AppsScript] Fallback parse failed: $e2")
                    }
                }
                
                return Result.Error(
                    AppError.Unknown("JSON parse failed: ${e.message}")
                )
            }

            if (parsed.success && parsed.data != null) {
                Result.Success(parsed.data)
            } else {
                Result.Error(
                    AppError.Unknown(parsed.error ?: "Server returned success=false")
                )
            }
        } catch (e: Exception) {
            val msg = "${e::class.simpleName}: ${e.message}"
            println("[AppsScript] ← ERROR: $msg")
            
            // Handle some common network exceptions by name to avoid JVM-specific imports
            when (e::class.simpleName) {
                "UnknownHostException" -> Result.Error(AppError.Network)
                "SocketTimeoutException", "ConnectTimeoutException", "HttpRequestTimeoutException" -> 
                    Result.Error(AppError.Unknown("Connection timed out"))
                else -> Result.Error(AppError.Unknown(msg))
            }
        }
    }

    private suspend fun executeUnit(
        request: AppsScriptRequest,
    ): Result<Unit> {
        return try {
            val url = buildUrl(request)
            println("[AppsScript] → GET $url")

            val response = httpClient.get(url)
            val rawBody: String = response.body()

            println("[AppsScript] ← Status: ${response.status}")
            println("[AppsScript] ← Raw: ${rawBody.take(400)}")

            if (!response.status.isSuccess()) {
                return Result.Error(
                    AppError.Unknown(httpErrorHint(response.status.value, rawBody))
                )
            }

            val parsed = try {
                json.decodeFromString<AppsScriptResponse<JsonElement?>>(rawBody)
            } catch (e: Exception) {
                return Result.Error(
                    AppError.Unknown("JSON parse failed: ${e.message}")
                )
            }

            if (parsed.success) {
                Result.Success(Unit)
            } else {
                Result.Error(
                    AppError.Unknown(parsed.error ?: "Server returned success=false")
                )
            }
        } catch (e: Exception) {
            val msg = "${e::class.simpleName}: ${e.message}"
            println("[AppsScript] ← ERROR: $msg")

            // Handle some common network exceptions by name to avoid JVM-specific imports
            when (e::class.simpleName) {
                "UnknownHostException" -> Result.Error(AppError.Network)
                "SocketTimeoutException", "ConnectTimeoutException", "HttpRequestTimeoutException" -> 
                    Result.Error(AppError.Unknown("Connection timed out"))
                else -> Result.Error(AppError.Unknown(msg))
            }
        }
    }

    private fun httpErrorHint(status: Int, rawBody: String): String {
        val hint = when (status) {
            401 -> "Apps Script requires authentication. Redeploy with 'Who has access: Anyone'."
            403 -> "Apps Script forbidden. Check deployment permissions."
            404 -> "Apps Script URL not found. Check WEB_APP_URL."
            500, 502, 503 -> "Apps Script crashed. Check Executions tab in script editor."
            else -> "Unexpected HTTP $status"
        }
        return "$hint (raw: ${rawBody.take(150)})"
    }
}
