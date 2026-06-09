package com.dnd.helper.presentation.desktop

import kotlinx.serialization.Serializable

@Serializable
data class Session(
    val id: String,
    val name: String,
)

data class SessionsState(
    val sessions: List<Session> = emptyList(),
    val activeTableId: String = "",
    val isImporting: Boolean = false,
    val importError: String? = null
)
