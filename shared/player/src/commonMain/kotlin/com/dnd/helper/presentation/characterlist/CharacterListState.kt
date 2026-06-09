package com.dnd.helper.presentation.characterlist

import com.dnd.helper.domain.model.Character

data class CharacterListState(
    val characters: List<Character> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)
