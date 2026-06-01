package com.dnd.helper.presentation.start

import androidx.lifecycle.ViewModel
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StartViewModel(
    private val storage: CharacterStorage
) : ViewModel() {
    private val _state = MutableStateFlow(
        StartState(
            characterId = storage.getCharacterId() ?: "",
            tableId = storage.getTableId() ?: ""
        )
    )
    val state = _state.asStateFlow()

    fun onEvent(event: StartEvent) {
        when (event) {
            is StartEvent.CharacterIdChanged -> {
                _state.value = _state.value.copy(characterId = event.id)
            }
            is StartEvent.TableIdChanged -> {
                _state.value = _state.value.copy(tableId = event.id)
            }
            StartEvent.LoadCharacter -> {
                if (_state.value.characterId.isNotBlank()) {
                    storage.saveCharacterId(_state.value.characterId)
                }
                if (_state.value.tableId.isNotBlank()) {
                    storage.saveTableId(_state.value.tableId)
                }
            }
        }
    }
}
