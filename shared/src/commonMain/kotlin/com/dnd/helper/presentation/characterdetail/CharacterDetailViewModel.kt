package com.dnd.helper.presentation.characterdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterDetailViewModel(
    private val repository: CharacterRepository,
    private val characterId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterDetailState())
    val state: StateFlow<CharacterDetailState> = _state.asStateFlow()

    init {
        loadCharacter()
    }

    fun onEvent(event: CharacterDetailEvent) {
        when (event) {
            CharacterDetailEvent.Refresh -> loadCharacter()
            is CharacterDetailEvent.UpdateStat -> updateStat(event.statName, event.delta)
            is CharacterDetailEvent.UpdateHp -> updateHp(event.delta)
            is CharacterDetailEvent.UpdateLevel -> updateLevel(event.delta)
            CharacterDetailEvent.ToggleEdit -> {
                val state = _state.value
                if (state.isEditing) {
                    _state.value = state.copy(isEditing = false, editedCharacter = null)
                } else {
                    _state.value = state.copy(isEditing = true, editedCharacter = state.character)
                }
            }
            is CharacterDetailEvent.EditCharacter -> {
                _state.value = _state.value.copy(editedCharacter = event.character)
            }
            CharacterDetailEvent.SaveChanges -> saveChanges()
        }
    }

    private fun saveChanges() {
        val edited = _state.value.editedCharacter ?: return
        _state.value = _state.value.copy(isSaving = true)
        
        viewModelScope.launch {
            when (val result = repository.saveCharacter(edited)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        character = edited,
                        isEditing = false,
                        editedCharacter = null,
                        isSaving = false
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        error = "Failed to save changes: ${result.error}",
                        isSaving = false
                    )
                }
            }
        }
    }

    private fun updateLevel(delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val updatedCharacter = currentCharacter.copy(
            level = (currentCharacter.level + delta).coerceAtLeast(1)
        )
        saveCharacter(updatedCharacter)
    }

    private fun updateStat(statName: String, delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val stats = currentCharacter.stats
        val newStats = when (statName.lowercase()) {
            "strength" -> stats.copy(strength = stats.strength + delta)
            "dexterity" -> stats.copy(dexterity = stats.dexterity + delta)
            "constitution" -> stats.copy(constitution = stats.constitution + delta)
            "intelligence" -> stats.copy(intelligence = stats.intelligence + delta)
            "wisdom" -> stats.copy(wisdom = stats.wisdom + delta)
            "charisma" -> stats.copy(charisma = stats.charisma + delta)
            else -> stats
        }
        val updatedCharacter = currentCharacter.copy(stats = newStats)
        saveCharacter(updatedCharacter)
    }

    private fun updateHp(delta: Int) {
        val currentCharacter = _state.value.character ?: return
        val updatedCharacter = currentCharacter.copy(
            currentHp = (currentCharacter.currentHp + delta).coerceIn(0, currentCharacter.maxHp)
        )
        saveCharacter(updatedCharacter)
    }

    private fun saveCharacter(character: com.dnd.helper.domain.model.Character) {
        // Optimistic update
        val previousCharacter = _state.value.character
        _state.value = _state.value.copy(character = character)

        viewModelScope.launch {
            when (val result = repository.saveCharacter(character)) {
                is Result.Success -> {
                    // Success, state is already updated
                }
                is Result.Error -> {
                    // Rollback on error
                    _state.value = _state.value.copy(
                        character = previousCharacter,
                        error = "Failed to update: ${result.error}"
                    )
                }
            }
        }
    }

    private fun loadCharacter() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.getCharacter(characterId)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        character = result.data,
                        isLoading = false,
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        error = result.error.toString(),
                        isLoading = false,
                    )
                }
            }
        }
    }
}
