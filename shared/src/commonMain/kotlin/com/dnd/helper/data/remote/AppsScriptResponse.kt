package com.dnd.helper.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class AppsScriptResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: String? = null,
)
