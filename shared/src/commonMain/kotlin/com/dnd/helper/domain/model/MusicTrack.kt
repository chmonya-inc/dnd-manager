package com.dnd.helper.domain.model

import com.dnd.helper.domain.utils.ImageUrlHelper
import kotlinx.serialization.Serializable

@Serializable
data class MusicTrack(
    val id: String,
    val name: String,
    val url: String
) {
    val downloadUrl: String? get() = ImageUrlHelper.process(url)
}
