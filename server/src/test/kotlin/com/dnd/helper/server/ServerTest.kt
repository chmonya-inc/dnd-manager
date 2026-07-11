package com.dnd.helper.server

import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.InitialData
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

    @Ignore
    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("D&D Helper Server is running!", response.bodyAsText())
    }

    @Ignore
    @Test
    fun testGetInitialData() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/api/test-session/initial-data")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val body = json.decodeFromString<InitialData>(response.bodyAsText())
        assertNotNull(body)
    }

    @Ignore
    @Test
    fun testSaveAndGetCharacter() = testApplication {
        application {
            module()
        }

        val character = Character(
            id = "test-char",
            name = "Test Hero",
            playerName = "Player 1",
            race = "Elf",
            characterClass = "Mage",
            level = 1,
            description = "A brave hero",
            maxHp = 10,
            currentHp = 10
        )

        // Save
        val saveResponse = client.post("/api/test-session/characters") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(json.encodeToString(character))
        }
        assertEquals(HttpStatusCode.OK, saveResponse.status)

        // Get
        val getResponse = client.get("/api/test-session/characters/test-char")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val body = json.decodeFromString<Character>(getResponse.bodyAsText())
        assertEquals("Test Hero", body.name)
    }

    @Ignore
    @Test
    fun testSessionIsolation() = testApplication {
        application {
            module()
        }

        val charA = Character(
            id = "char-1",
            name = "Hero A",
            playerName = "P1",
            race = "R1",
            characterClass = "C1",
            level = 1,
            description = "D1",
            maxHp = 10,
            currentHp = 10
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
            currentHp = 10
        )

        // Save to Session A
        client.post("/api/session-a/characters") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(json.encodeToString(charA))
        }

        // Save to Session B
        client.post("/api/session-b/characters") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(json.encodeToString(charB))
        }

        // Get from Session A
        val getAResponse = client.get("/api/session-a/characters/char-1")
        assertEquals("Hero A", json.decodeFromString<Character>(getAResponse.bodyAsText()).name)

        // Get from Session B
        val getBResponse = client.get("/api/session-b/characters/char-1")
        assertEquals("Hero B", json.decodeFromString<Character>(getBResponse.bodyAsText()).name)
    }
}
