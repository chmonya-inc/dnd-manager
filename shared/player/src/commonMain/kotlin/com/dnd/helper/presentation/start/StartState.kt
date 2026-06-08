package com.dnd.helper.presentation.start

data class StartState(
    val characterId: String = "",
    val tableId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
