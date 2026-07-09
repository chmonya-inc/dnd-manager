package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

data class SettingsState(
    val serverAddress: String = "http://localhost:9090",
    val comfyUiAddress: String = "127.0.0.1:8000",
    val hasWorkflow: Boolean = false,
    val generationSteps: Int = 20
)

class SettingsViewModel(
    private val storage: CharacterStorage,
    private val authRepository: com.dnd.helper.domain.repository.AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsState(
            serverAddress = storage.getServerAddress() ?: "http://localhost:9090",
            comfyUiAddress = storage.getComfyUiAddress() ?: "127.0.0.1:8000",
            hasWorkflow = storage.getComfyUiWorkflow() != null,
            generationSteps = storage.getGenerationSteps()
        )
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun updateServerAddress(address: String) {
        storage.saveServerAddress(address)
        _state.value = _state.value.copy(serverAddress = address)
    }

    fun updateComfyUiAddress(address: String) {
        storage.saveComfyUiAddress(address)
        _state.value = _state.value.copy(comfyUiAddress = address)
    }

    fun updateComfyUiWorkflow(jsonStr: String) {
        try {
            val workflow = Json.decodeFromString<JsonObject>(jsonStr)
            storage.saveComfyUi(workflow)
            _state.value = _state.value.copy(hasWorkflow = true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun updateGenerationSteps(steps: Int) {
        storage.saveGenerationSteps(steps)
        _state.value = _state.value.copy(generationSteps = steps)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

