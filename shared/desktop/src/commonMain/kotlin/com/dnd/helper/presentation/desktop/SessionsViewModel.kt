package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.IdUtils
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.storage.CharacterStorage
import com.dnd.helper.data.remote.KtorRemoteDataSource
import com.dnd.helper.domain.common.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class SessionsViewModel(
    private val storage: CharacterStorage,
    private val remoteDataSource: KtorRemoteDataSource
) : ViewModel() {

    private val _state = MutableStateFlow(
        SessionsState(
            activeTableId = storage.getTableId() ?: ""
        )
    )
    val state = _state.asStateFlow()

    init {
        loadCampaigns()
    }

    fun loadCampaigns() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = remoteDataSource.getCampaigns()) {
                is Result.Success -> {
                    val campaigns = res.data.map { Campaign(id = it.sessionId, name = it.name) }
                    _state.value = _state.value.copy(
                        campaigns = campaigns,
                        isLoading = false
                    )
                    
                    // Auto-select logic
                    val currentActive = storage.getTableId()
                    if (!currentActive.isNullOrBlank() && campaigns.any { it.id == currentActive }) {
                        selectCampaignForPreview(currentActive)
                    } else if (campaigns.isNotEmpty()) {
                        selectCampaignForPreview(campaigns.first().id)
                    }
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = res.error.toUserMessage()
                    )
                }
            }
        }
    }

    fun selectCampaign(id: String) {
        storage.saveTableId(id)
        _state.value = _state.value.copy(activeTableId = id)
    }

    fun selectCampaignForPreview(id: String) {
        if (_state.value.previewCampaignId == id) return
        
        _state.value = _state.value.copy(
            previewCampaignId = id,
            isPreviewLoading = true,
            previewData = null
        )
        
        viewModelScope.launch {
            // Temporarily set table ID in storage to fetch initial data for preview
            val oldId = storage.getTableId()
            storage.saveTableId(id)
            
            when (val res = remoteDataSource.getInitialData()) {
                is Result.Success -> {
                    if (_state.value.previewCampaignId == id) {
                        _state.value = _state.value.copy(
                            previewData = res.data,
                            isPreviewLoading = false
                        )
                    }
                }
                is Result.Error -> {
                    if (_state.value.previewCampaignId == id) {
                        _state.value = _state.value.copy(isPreviewLoading = false)
                    }
                }
            }
            
            // Restore old ID if we didn't officially "switch" yet
            if (oldId != null) storage.saveTableId(oldId) else storage.saveTableId("")
        }
    }

    fun addCampaign(name: String, joinId: String) {
        val finalId = if (joinId.isNotBlank()) {
            IdUtils.decode(joinId)
        } else {
            IdUtils.generateSessionId()
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val res = remoteDataSource.createCampaign(name, finalId)
            when (res) {
                is Result.Success -> {
                    loadCampaigns() // Refresh list
                    if (_state.value.activeTableId.isEmpty()) {
                        selectCampaign(finalId)
                    }
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = res.error.toUserMessage()
                    )
                }
            }
        }
    }

    fun deleteCampaignLocal(id: String) {
        // Since we are not deleting on server for now, just remove from view?
        // Actually, the requirement was to show sessions owned by master.
        // If master "deletes" it should probably be deleted on server too.
        // For now, let's just filter it out of the local state if we don't have a server delete yet.
        val updated = _state.value.campaigns.filter { it.id != id }
        _state.value = _state.value.copy(
            campaigns = updated,
            activeTableId = if (_state.value.activeTableId == id) "" else _state.value.activeTableId
        )
        if (_state.value.activeTableId == id) {
            storage.saveTableId("")
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
