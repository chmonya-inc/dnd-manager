package com.dnd.helper.domain.model

import com.dnd.helper.domain.utils.ImageUrlHelper
import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null
) {
    val displayImageUrl: String? get() = ImageUrlHelper.process(imageUrl)
}
