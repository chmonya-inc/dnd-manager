package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.IdUtils
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.storage.CharacterStorage
import com.dnd.helper.data.import.SessionImporter
import com.dnd.helper.data.remote.KtorRemoteDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SessionsViewModel(
    private val storage: CharacterStorage,
    private val repository: CharacterRepository,
    private val remoteDataSource: KtorRemoteDataSource
) : ViewModel() {

    private val json = Json { ignoreUnknownKeys = true }
    
    private val _state = MutableStateFlow(
        SessionsState(
            sessions = loadSessions(),
            activeTableId = storage.getTableId() ?: ""
        )
    )
    val state = _state.asStateFlow()

    private fun loadSessions(): List<Session> {
        val raw = storage.getSessions() ?: return emptyList()
        return try {
            json.decodeFromString<List<Session>>(raw)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveSessions(sessions: List<Session>) {
        storage.saveSessions(json.encodeToString(sessions))
    }

    fun selectSession(id: String) {
        storage.saveTableId(id)
        _state.value = _state.value.copy(activeTableId = id)
    }

    fun deleteSession(id: String) {
        val updated = _state.value.sessions.filter { it.id != id }
        _state.value = _state.value.copy(
            sessions = updated,
            activeTableId = if (_state.value.activeTableId == id) "" else _state.value.activeTableId
        )
        saveSessions(updated)
        if (_state.value.activeTableId == id) {
            storage.saveTableId("")
        }
    }

    fun addSession(name: String, joinId: String) {
        val finalId = if (joinId.isNotBlank()) {
            IdUtils.decode(joinId)
        } else {
            IdUtils.generateSessionId()
        }
        
        val updated = _state.value.sessions.toMutableList()
        updated.removeAll { it.id == finalId }
        updated.add(Session(id = finalId, name = name))
        
        _state.value = _state.value.copy(sessions = updated)
        saveSessions(updated)

        // Also register as a campaign on the server (linked to the logged-in master)
        viewModelScope.launch {
            remoteDataSource.createCampaign(name, finalId)
        }
    }

    fun importData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isImporting = true, importError = null)
            when (val res = SessionImporter.import(repository)) {
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        importError = "Import failed: ${res.error}",
                        isImporting = false
                    )
                }
                is Result.Success -> {
                    _state.value = _state.value.copy(isImporting = false)
                }
            }
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(importError = null)
    }
}
