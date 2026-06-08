package com.dnd.helper.domain.utils

object TimeUtils {
    fun now(): String {
        // Simple platform-agnostic ISO-like timestamp
        // Since we can't easily use java.time or kotlinx-datetime without sync,
        // we use a simple format for now.
        return "2024-05-20T12:00:00Z" // Placeholder, in real app use proper date
    }
}
