package com.dnd.helper.domain.model

import com.dnd.helper.domain.utils.ImageUrlHelper
import kotlinx.serialization.Serializable

@Serializable
data class Monster(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val stats: CharacterStats = CharacterStats(),
    val maxHp: Int = 10,
    val currentHp: Int = 10,
    val armorClass: Int = 10,
    val speed: Int = 30,
    val challengeRating: String = "1",
    val type: String = "Humanoid",
    val alignment: String = "Neutral",
    val size: String = "Medium"
) {
    val displayImageUrl: String? get() = ImageUrlHelper.process(imageUrl)
}
