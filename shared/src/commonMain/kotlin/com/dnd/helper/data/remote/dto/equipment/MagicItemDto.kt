package com.dnd.helper.data.remote.dto.equipment

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.Serializable

@Serializable
data class MagicItemDto(
    val index: String = "",
    val name: String = "",
    val desc: List<String> = emptyList(),
    val equipment_category: ApiReferenceDto = ApiReferenceDto(),
    val rarity: MagicItemRarityDto = MagicItemRarityDto(),
    val variants: List<ApiReferenceDto> = emptyList(),
    val variant: Boolean = false,
    val image: String? = null,
    val url: String = "",
)

/**
 * Rarity name values: "Varies" | "Common" | "Uncommon" | "Rare" | "Very Rare" | "Legendary" | "Artifact"
 */
@Serializable
data class MagicItemRarityDto(
    val name: String = "Common",
)
