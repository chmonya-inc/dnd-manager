package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsState(
    val comfyUiAddress: String = "127.0.0.1:8000",
    val hasWorkflow: Boolean = false,
    val generationSteps: Int = 20
)

class SettingsViewModel(
    private val storage: CharacterStorage
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            comfyUiAddress = storage.getComfyUiAddress() ?: "127.0.0.1:8000",
            hasWorkflow = storage.getComfyUiWorkflow() != null,
            generationSteps = storage.getGenerationSteps()
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun updateComfyUiAddress(address: String) {
        storage.saveComfyUiAddress(address)
        _state.value = _state.value.copy(comfyUiAddress = address)
    }

    fun updateComfyUiWorkflow(json: String) {
        storage.saveComfyUiWorkflow(json)
        _state.value = _state.value.copy(hasWorkflow = true)
    }

    fun updateGenerationSteps(steps: Int) {
        storage.saveGenerationSteps(steps)
        _state.value = _state.value.copy(generationSteps = steps)
    }
}
