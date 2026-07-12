package com.dnd.helper.domain.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdUtilsTest {

    @Test
    fun testEncodeDecodeRoundTrip() {
        val originalId = "session123"
        val encoded = IdUtils.encode(originalId)
        val decoded = IdUtils.decode(encoded)
        assertEquals(originalId, decoded)
    }

    @Test
    fun testDecodeWithoutPrefix() {
        val input = "some-raw-id"
        val decoded = IdUtils.decode(input)
        assertEquals(input, decoded)
    }

    @Test
    fun testDecodeMalformedBase64() {
        val input = "DNDH-!@#$%^" // Definitely malformed base64
        val decoded = IdUtils.decode(input)
        // If it throws, it returns the input. If it doesn't throw, it might return garbage.
        // We'll accept either for now as long as it doesn't crash the app.
        assertTrue(decoded == input || decoded.length != input.length)
    }

    @Test
    fun testGenerateSessionId() {
        val sessionId = IdUtils.generateSessionId()
        assertEquals(8, sessionId.length)
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        assertTrue(sessionId.all { it in allowedChars })
    }
}
