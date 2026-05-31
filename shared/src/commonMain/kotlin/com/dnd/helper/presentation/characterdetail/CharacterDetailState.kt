package com.dnd.helper.presentation.characterdetail

import com.dnd.helper.domain.model.Character

data class CharacterDetailState(
    val character: Character? = null,
    val editedCharacter: Character? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    /** True when stat/HP/level changes are queued for debounced save. */
    val hasUnsavedChanges: Boolean = false,
)
