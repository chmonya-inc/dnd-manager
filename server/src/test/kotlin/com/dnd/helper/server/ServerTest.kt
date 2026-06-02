package com.dnd.helper.server

import com.dnd.helper.data.remote.AppsScriptRequest
import com.dnd.helper.data.remote.AppsScriptResponse
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.InitialData
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class ServerTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("D&D Helper Server is running!", response.bodyAsText())
    }

    @Test
    fun testGetInitialData() = testApplication {
        application {
            module()
        }
        
        val response = client.get("/exec?request={\"action\":\"getInitialData\"}")
        assertEquals(HttpStatusCode.OK, response.status)
        
        val bodyText = response.bodyAsText()
        val body = Json.decodeFromString<AppsScriptResponse<InitialData>>(bodyText)
        assertTrue(body.success)
        assertNotNull(body.data)
    }

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
        val saveResponse = client.post("/exec") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(Json.encodeToString(AppsScriptRequest.serializer(), AppsScriptRequest(action = "saveCharacter", character = character)))
        }
        assertEquals(HttpStatusCode.OK, saveResponse.status)
        val saveBody = Json.decodeFromString<AppsScriptResponse<Unit>>(saveResponse.bodyAsText())
        assertTrue(saveBody.success)

        // Get
        val getResponse = client.get("/exec?request={\"action\":\"getCharacter\",\"id\":\"test-char\"}")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val getBody = Json.decodeFromString<AppsScriptResponse<Character>>(getResponse.bodyAsText())
        assertTrue(getBody.success)
        assertEquals("Test Hero", getBody.data?.name)
    }
}
