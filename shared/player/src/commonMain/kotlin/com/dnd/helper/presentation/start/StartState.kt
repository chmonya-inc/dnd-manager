package com.dnd.helper.presentation.start

import com.dnd.helper.data.remote.dto.auth.MyCharacterDto

data class StartState(
    val characterId: String = "",
    val tableId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val myCharacters: List<MyCharacterDto> = emptyList(),
    val isLoadingMyCharacters: Boolean = false,
    val username: String? = null,
    val isMaster: Boolean = false
)
