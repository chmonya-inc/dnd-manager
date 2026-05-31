package com.dnd.helper.presentation.characterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CharacterListViewModel(
    private val repository: CharacterRepository,
    private val storage: CharacterStorage
) : ViewModel() {

    private val _state = MutableStateFlow(CharacterListState())
    val state: StateFlow<CharacterListState> = _state.asStateFlow()

    /** Tracks the server's last-modified timestamp to detect external changes. */
    private var lastKnownTimestamp: String? = null

    /** Active polling job; null when not polling. */
    private var pollingJob: Job? = null

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = repository.getInitialData()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        characters = result.data.characters,
                        isLoading = false,
                    )
                    lastKnownTimestamp = result.data.lastModified
                }
                is Result.Error -> {
                    // Fallback to separate loading if bulk loading fails
                    loadCharacters()
                }
            }
        }
    }

    fun onEvent(event: CharacterListEvent) {
        when (event) {
            CharacterListEvent.Refresh -> loadCharacters()
            is CharacterListEvent.CharacterClicked -> {
                storage.saveCharacterId(event.characterId)
            }
        }
    }

    /** Starts auto-refresh polling. Call from DisposableEffect/onResume. */
    fun startAutoRefresh(intervalMs: Long = 4_000L) {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(intervalMs)
                checkForUpdates()
            }
        }
    }

    /** Stops auto-refresh polling. Call from DisposableEffect/onDispose/onPause. */
    fun stopAutoRefresh() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun checkForUpdates() {
        when (val result = repository.getLastModified()) {
            is Result.Success -> {
                val serverTimestamp = result.data
                if (lastKnownTimestamp != null && lastKnownTimestamp != serverTimestamp) {
                    println("[AutoRefresh] Character list changed on server ($lastKnownTimestamp → $serverTimestamp), reloading...")
                    loadCharacters(fromAutoRefresh = true)
                }
                lastKnownTimestamp = serverTimestamp
            }
            is Result.Error -> {
                // Silently ignore polling errors so the UI isn't noisy.
                println("[AutoRefresh] Polling error: ${result.error}")
            }
        }
    }

    private fun loadCharacters(fromAutoRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!fromAutoRefresh) {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }
            when (val result = repository.getCharacters()) {
                is Result.Success -> {
                    _state.value = _state.value.copy(
                        characters = result.data,
                        isLoading = false,
                    )
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.error.toUserMessage(),
                    )
                }
            }
        }
    }
}
