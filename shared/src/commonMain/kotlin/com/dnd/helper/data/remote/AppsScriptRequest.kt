package com.dnd.helper.data.remote

import com.dnd.helper.domain.model.Character
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AppsScriptRequest(
    val action: String,
    val id: String? = null,
    val character: Character? = null,
    val payload: JsonObject? = null,
)
