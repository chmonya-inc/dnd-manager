package com.dnd.helper.data.remote.dto.equipment

import kotlinx.serialization.Serializable

@Serializable
data class WeaponPropertyDto(
    val index: String = "",
    val name: String = "",
    val desc: List<String> = emptyList(),
    val url: String = "",
)
