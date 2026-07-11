package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.data.remote.KtorRemoteDataSource
import com.dnd.helper.domain.common.IdUtils
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.common.toUserMessage
import com.dnd.helper.domain.repository.CharacterRepository
import com.dnd.helper.domain.storage.CharacterStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SessionsViewModel(
    private val storage: CharacterStorage,
    private val remoteDataSource: KtorRemoteDataSource,
    private val characterRepository: CharacterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        SessionsState(
            activeTableId = storage.getTableId() ?: ""
        )
    )
    val state = _state.asStateFlow()

    init {
        loadCampaigns()
        observeWebSocketUpdates()
        startPolling()
    }

    /**
     * Observe WebSocket updates — react to character changes
     * by refreshing the campaign preview and campaign list.
     */
    private fun observeWebSocketUpdates() {
        viewModelScope.launch {
            characterRepository.remoteUpdates.collect { updateMessage ->
                val updateType = updateMessage.split(":").firstOrNull() ?: return@collect
                when (updateType) {
                    "characters" -> {
                        refreshCampaignsSilently()
                        refreshPreviewSilently()
                    }
                }
            }
        }
    }

    /**
     * Periodic polling every 2 seconds — silently refreshes campaigns
     * and preview data without showing loading indicators.
     */
    private fun startPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(2_000)
                refreshCampaignsSilently()
                refreshPreviewSilently()
            }
        }
    }

    /**
     * Silently refresh campaigns without showing loading indicator.
     * Only updates if data has changed.
     */
    private fun refreshCampaignsSilently() {
        viewModelScope.launch {
            when (val res = remoteDataSource.getCampaigns()) {
                is Result.Success -> {
                    val campaigns = res.data.map {
                        Campaign(
                            id = it.sessionId,
                            name = it.name,
                            isStarted = it.isStarted
                        )
                    }
                    val current = _state.value.campaigns
                    if (current.size != campaigns.size ||
                        current.zip(campaigns).any { (old, new) ->
                            old.id != new.id ||
                                old.name != new.name ||
                                old.isStarted != new.isStarted
                        }
                    ) {
                        _state.value = _state.value.copy(campaigns = campaigns)

                        // Auto-select if not already selected
                        if (_state.value.previewCampaignId == null && campaigns.isNotEmpty()) {
                            val currentActive = storage.getTableId()
                            if (!currentActive.isNullOrBlank() && campaigns.any { it.id == currentActive }) {
                                selectCampaignForPreview(currentActive)
                            } else {
                                selectCampaignForPreview(campaigns.first().id)
                            }
                        }
                    }
                }
                is Result.Error -> {}
            }
        }
    }

    /**
     * Silently refresh the preview data without showing loading indicator.
     */
    private fun refreshPreviewSilently() {
        val previewId = _state.value.previewCampaignId ?: return
        viewModelScope.launch {
            val oldId = storage.getTableId()
            storage.saveTableId(previewId)
            when (val res = remoteDataSource.getInitialData()) {
                is Result.Success -> {
                    if (_state.value.previewCampaignId == previewId) {
                        _state.value = _state.value.copy(previewData = res.data)
                    }
                }
                is Result.Error -> {}
            }
            if (oldId != null) storage.saveTableId(oldId) else storage.saveTableId("")
        }
    }

    fun loadCampaigns() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val res = remoteDataSource.getCampaigns()) {
                is Result.Success -> {
                    val campaigns = res.data.map {
                        Campaign(
                            id = it.sessionId,
                            name = it.name,
                            isStarted = it.isStarted
                        )
                    }
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

    fun toggleCampaignStart(id: String, isStarted: Boolean) {
        // Optimistic update: reflect the change immediately
        _state.value = _state.value.copy(
            campaigns = _state.value.campaigns.map {
                if (it.id == id) it.copy(isStarted = isStarted) else it
            }
        )
        viewModelScope.launch {
            val res = remoteDataSource.toggleCampaignStart(id, isStarted)
            if (res is Result.Success) {
                // Silently refresh to confirm server state
                refreshCampaignsSilently()
            } else {
                // Revert on failure
                _state.value = _state.value.copy(
                    campaigns = _state.value.campaigns.map {
                        if (it.id == id) it.copy(isStarted = !isStarted) else it
                    },
                    error = (res as Result.Error).error.toUserMessage()
                )
            }
        }
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

    fun transferOrCopyCharacter(
        character: com.dnd.helper.domain.model.Character,
        sourceSessionId: String,
        targetCampaign: Campaign,
        isCopy: Boolean,
        transferItems: Boolean
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isTransferring = true, error = null)

            // Build the character for the target session
            val targetCharacter = if (isCopy) {
                character.copy(
                    id = com.dnd.helper.domain.common.IdUtils.generateSessionId(), // new ID for copy
                    ownerUserId = null,
                    ownerUsername = null
                )
            } else {
                character.copy()
            }

            val finalCharacter = if (!transferItems) {
                targetCharacter.copy(items = emptyList())
            } else {
                targetCharacter
            }

            // 1. Save character to target session
            val saveResult = remoteDataSource.saveCharacterToSession(finalCharacter, targetCampaign.id)
            if (saveResult is Result.Error) {
                _state.value = _state.value.copy(
                    isTransferring = false,
                    error = saveResult.error.toUserMessage()
                )
                return@launch
            }

            // 2. If move (not copy), delete from source session
            if (!isCopy) {
                val deleteResult = remoteDataSource.deleteCharacterFromSession(character.id, sourceSessionId)
                if (deleteResult is Result.Error) {
                    _state.value = _state.value.copy(
                        isTransferring = false,
                        error = deleteResult.error.toUserMessage()
                    )
                    return@launch
                }
            }

            _state.value = _state.value.copy(isTransferring = false)

            // 3. Refresh preview data to reflect the change
            selectCampaignForPreview(sourceSessionId)
        }
    }
}
