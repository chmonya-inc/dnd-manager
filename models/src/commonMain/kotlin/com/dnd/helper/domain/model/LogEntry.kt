package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LogEntry(
    val timestamp: String? = null,
    val action: String,
    val details: String? = null,
    val initialState: String? = null,
    val endState: String? = null,
    val success: Boolean = true
)
