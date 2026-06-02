package com.dnd.helper.domain.common

import io.ktor.util.decodeBase64String
import io.ktor.util.encodeBase64

object IdUtils {
    private const val PREFIX = "DNDH-"

    fun encode(id: String): String {
        return PREFIX + id.encodeToByteArray().encodeBase64()
    }

    fun decode(input: String): String {
        val trimmed = input.trim()
        if (trimmed.startsWith(PREFIX)) {
            return try {
                trimmed.substring(PREFIX.length).decodeBase64String()
            } catch (e: Exception) {
                trimmed
            }
        }
        return trimmed
    }

    fun generateSessionId(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..8)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
