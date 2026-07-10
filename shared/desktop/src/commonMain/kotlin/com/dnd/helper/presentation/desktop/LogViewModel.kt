package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.LogEntry
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class LogState(
    val logs: List<LogEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class LogViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LogState())
    val state: StateFlow<LogState> = _state.asStateFlow()

    init {
        refreshLogs()

        // Listen for remote updates via WebSocket
        viewModelScope.launch {
            repository.remoteUpdates.collect { updateType ->
                if (updateType == "logs") {
                    println("[Logs] Remote update received via WebSocket, reloading logs...")
                    refreshLogs(force = true)
                }
            }
        }
    }

    fun refreshLogs(force: Boolean = false) {
        viewModelScope.launch {
            if (!force) _state.value = _state.value.copy(isLoading = true)
            val result = repository.getLogs()
            if (result is Result.Success) {
                _state.value = _state.value.copy(logs = result.data, isLoading = false)
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun undoLog(log: LogEntry) {
        viewModelScope.launch {
            val initialState = log.initialState
            if (!initialState.isNullOrBlank() && log.action.contains("Character")) {
                try {
                    val oldCharacter = Json.decodeFromString<Character>(initialState)
                    val result = repository.saveCharacter(oldCharacter)
                    if (result is Result.Success) {
                        repository.saveLog(
                            LogEntry(
                                action = "Undo: ${log.action}",
                                details = "Reverted to state from ${log.timestamp}",
                                success = true
                            )
                        )
                        refreshLogs(force = true)
                    }
                } catch (e: Exception) {
                    println("Undo failed: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
}
