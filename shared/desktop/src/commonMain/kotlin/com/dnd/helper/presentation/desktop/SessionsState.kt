package com.dnd.helper.presentation.desktop

import kotlinx.serialization.Serializable

@Serializable
data class Campaign(
    val id: String,
    val name: String,
)

data class SessionsState(
    val campaigns: List<Campaign> = emptyList(),
    val activeTableId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
