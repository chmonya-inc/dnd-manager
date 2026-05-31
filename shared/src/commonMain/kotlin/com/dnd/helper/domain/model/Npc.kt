package com.dnd.helper.domain.model

import com.dnd.helper.domain.utils.ImageUrlHelper
import kotlinx.serialization.Serializable

@Serializable
data class Npc(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val background: String = ""
) {
    val displayImageUrl: String? get() = ImageUrlHelper.process(imageUrl)
}
