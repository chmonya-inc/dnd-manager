package com.dnd.helper.server

import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.InitialData
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ServerTest {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    @BeforeTest
    fun setup() {
        TestDatabase.start()
    }

    private suspend inline fun <reified T> HttpClient.authenticatedPost(url: String, token: String, body: T) = post(url) {
        header(HttpHeaders.Authorization, "Bearer $token")
        header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        setBody(json.encodeToString(body))
    }

    private suspend fun HttpClient.authenticatedGet(url: String, token: String) = get(url) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    @Test
    @Ignore
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("D&D Helper Server is running!", response.bodyAsText())
    }

    @Test
    @Ignore
    fun testGetInitialData() = testApplication {
        application {
            module()
        }

        val auth = AuthTestHelper.registerUser(client, "testUser", "PLAYER")
        
        val response = client.authenticatedGet("/api/test-session/initial-data", auth.accessToken)
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = json.decodeFromString<InitialData>(response.bodyAsText())
        assertNotNull(body)
    }

    @Test
    @Ignore
    fun testSaveAndGetCharacter() = testApplication {
        application {
            module()
        }

        val auth = AuthTestHelper.registerUser(client, "testUser2", "PLAYER")

        val character = Character(
            id = "test-char",
            name = "Test Hero",
            playerName = "Player 1",
            race = "Elf",
            characterClass = "Mage",
            level = 1,
            description = "A brave hero",
            maxHp = 10,
            currentHp = 10,
        )

        // Save
        val saveResponse = client.authenticatedPost("/api/test-session/characters", auth.accessToken, character)
        assertEquals(HttpStatusCode.OK, saveResponse.status)

        // Get
        val getResponse = client.authenticatedGet("/api/test-session/characters/test-char", auth.accessToken)
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val body = json.decodeFromString<Character>(getResponse.bodyAsText())
        assertEquals("Test Hero", body.name)
    }

    @Test
    @Ignore
    fun testSessionIsolation() = testApplication {
        application {
            module()
        }

        val auth = AuthTestHelper.registerUser(client, "testUser3", "PLAYER")

        val charA = Character(
            id = "char-1",
            name = "Hero A",
            playerName = "P1",
            race = "R1",
            characterClass = "C1",
            level = 1,
            description = "D1",
            maxHp = 10,
            currentHp = 10,
        )

        val charB = Character(
            id = "char-1", // Same ID but different session
            name = "Hero B",
            playerName = "P2",
            race = "R2",
            characterClass = "C2",
            level = 1,
            description = "D2",
            maxHp = 10,
            currentHp = 10,
        )

        // Save to Session A
        client.authenticatedPost("/api/session-a/characters", auth.accessToken, charA)

        // Save to Session B
        client.authenticatedPost("/api/session-b/characters", auth.accessToken, charB)

        // Get from Session A
        val getAResponse = client.authenticatedGet("/api/session-a/characters/char-1", auth.accessToken)
        assertEquals("Hero A", json.decodeFromString<Character>(getAResponse.bodyAsText()).name)

        // Get from Session B
        val getBResponse = client.authenticatedGet("/api/session-b/characters/char-1", auth.accessToken)
        assertEquals("Hero B", json.decodeFromString<Character>(getBResponse.bodyAsText()).name)
    }
}
