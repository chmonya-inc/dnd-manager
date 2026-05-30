package com.dnd.helper.data.remote

import com.dnd.helper.data.config.GoogleAppsScriptConfig
import com.dnd.helper.domain.common.AppError
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.encodeURLQueryComponent
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class GoogleAppsScriptDataSource(
    private val httpClient: HttpClient,
) {

    suspend fun getCharacters(): Result<List<Character>> =
        execute(request = AppsScriptRequest(action = "getCharacters"))

    suspend fun getCharacter(id: String): Result<Character> =
        execute(request = AppsScriptRequest(action = "getCharacter", id = id))

    suspend fun saveCharacter(character: Character): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "saveCharacter", character = character))

    suspend fun deleteCharacter(id: String): Result<Unit> =
        executeUnit(request = AppsScriptRequest(action = "deleteCharacter", id = id))

    /**
     * Builds the request URL with the JSON payload encoded as a query parameter.
     *
     * Google Apps Script Web Apps return HTTP 302 for POST requests, converting
     * them to GET and dropping the body. By using GET directly with the request
     * JSON in the URL, we avoid the redirect issue entirely.
     */
    private fun buildUrl(request: AppsScriptRequest): String {
        val json = Json.encodeToString(request)
        val encoded = json.encodeURLQueryComponent()
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
                Json.decodeFromString<AppsScriptResponse<T>>(rawBody)
            } catch (e: Exception) {
                println(e)
                return Result.Error(
                    AppError.Unknown("e: $e, JSON parse failed: ${rawBody.take(200)}")
                )
            }

            if (parsed.success && parsed.data != null) {
                Result.Success(parsed.data)
            } else {
                Result.Error(
                    AppError.Unknown(parsed.error ?: "Server returned success=false")
                )
            }
        } catch (e: java.net.UnknownHostException) {
            Result.Error(AppError.Network)
        } catch (e: java.net.SocketTimeoutException) {
            Result.Error(AppError.Unknown("Connection timed out"))
        } catch (e: Exception) {
            val msg = "${e::class.simpleName}: ${e.message}"
            println("[AppsScript] ← ERROR: $msg")
            Result.Error(AppError.Unknown(msg))
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
                Json.decodeFromString<AppsScriptResponse<JsonObject?>>(rawBody)
            } catch (e: Exception) {
                return Result.Error(
                    AppError.Unknown("JSON parse failed: ${rawBody.take(200)}")
                )
            }

            if (parsed.success) {
                Result.Success(Unit)
            } else {
                Result.Error(
                    AppError.Unknown(parsed.error ?: "Server returned success=false")
                )
            }
        } catch (e: java.net.UnknownHostException) {
            Result.Error(AppError.Network)
        } catch (e: java.net.SocketTimeoutException) {
            Result.Error(AppError.Unknown("Connection timed out"))
        } catch (e: Exception) {
            val msg = "${e::class.simpleName}: ${e.message}"
            println("[AppsScript] ← ERROR: $msg")
            Result.Error(AppError.Unknown(msg))
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
