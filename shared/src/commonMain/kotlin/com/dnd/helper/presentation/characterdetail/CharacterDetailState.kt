package com.dnd.helper.presentation.characterdetail

import com.dnd.helper.domain.model.Character

data class CharacterDetailState(
    val character: Character? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
