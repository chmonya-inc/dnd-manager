package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long = 0,
    val isPublic: Boolean = true
)
