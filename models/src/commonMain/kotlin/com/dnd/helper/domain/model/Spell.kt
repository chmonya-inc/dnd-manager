package com.dnd.helper.domain.model

import com.dnd.helper.domain.utils.ImageUrlHelper
import kotlinx.serialization.Serializable

@Serializable
data class Spell(
    val id: String,
    val name: String,
    val description: String = "",
    val iconUrl: String? = null,
    val damage: String = "",
    val damageType: String = "",
    val resourceCost: String = "",
    val range: String = "",
    val castingTime: String = "",
    val duration: String = "",
    val level: Int = 0,
    val school: String = "",
    val isPassive: Boolean = false,
) {
    val displayIconUrl: String? get() = ImageUrlHelper.process(iconUrl)
}
