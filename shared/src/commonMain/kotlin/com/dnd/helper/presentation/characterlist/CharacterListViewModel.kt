package com.dnd.helper.presentation.characterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterListViewModel : ViewModel() {

    private val _state = MutableStateFlow(CharacterListState())
    val state: StateFlow<CharacterListState> = _state.asStateFlow()

    init {
        loadCharacters()
    }

    fun onEvent(event: CharacterListEvent) {
        when (event) {
            CharacterListEvent.Refresh -> loadCharacters()
            is CharacterListEvent.CharacterClicked -> {
                // TODO: Navigation will be added later
            }
        }
    }

    private fun loadCharacters() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                // Mock data — will be replaced with repository call
                val characters = getMockCharacters()
                _state.value = _state.value.copy(
                    characters = characters,
                    isLoading = false,
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message,
                )
            }
        }
    }

    private fun getMockCharacters(): List<Character> = listOf(
        Character(
            id = "1",
            name = "Thorin Ironfist",
            playerName = "Alex",
            race = "Dwarf",
            characterClass = "Fighter",
            level = 5,
            description = "A grizzled dwarf warrior seeking to reclaim his ancestral home.",
            maxHp = 45,
            currentHp = 38,
            stats = CharacterStats(
                strength = 16,
                dexterity = 12,
                constitution = 14,
                intelligence = 10,
                wisdom = 13,
                charisma = 8,
            ),
        ),
        Character(
            id = "2",
            name = "Lyra Moonwhisper",
            playerName = "Sarah",
            race = "Elf",
            characterClass = "Wizard",
            level = 4,
            description = "An elven scholar obsessed with ancient magic.",
            maxHp = 28,
            currentHp = 28,
            stats = CharacterStats(
                strength = 8,
                dexterity = 14,
                constitution = 12,
                intelligence = 16,
                wisdom = 13,
                charisma = 10,
            ),
        ),
        Character(
            id = "3",
            name = "Garrett Shadowstep",
            playerName = "Mike",
            race = "Human",
            characterClass = "Rogue",
            level = 5,
            description = "A charming thief with a heart of gold.",
            maxHp = 35,
            currentHp = 30,
            stats = CharacterStats(
                strength = 12,
                dexterity = 16,
                constitution = 12,
                intelligence = 14,
                wisdom = 10,
                charisma = 14,
            ),
        ),
    )
}
