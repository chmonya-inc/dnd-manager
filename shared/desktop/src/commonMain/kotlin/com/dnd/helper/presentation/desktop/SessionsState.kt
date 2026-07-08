package com.dnd.helper.presentation.desktop

import kotlinx.serialization.Serializable

@Serializable
data class Campaign(
    val id: String,
    val name: String,
)

data class SessionsState(
    val campaigns: List<Campaign> = emptyList(),
    val activeTableId: String = "",
    val previewCampaignId: String? = null,
    val previewData: com.dnd.helper.domain.model.InitialData? = null,
    val isLoading: Boolean = false,
    val isPreviewLoading: Boolean = false,
    val error: String? = null
)
