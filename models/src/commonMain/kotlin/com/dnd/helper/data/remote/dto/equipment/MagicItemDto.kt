package com.dnd.helper.data.remote.dto.equipment

import com.dnd.helper.data.remote.dto.common.ApiReferenceDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MagicItemDto(
    @SerialName("index") val index: String = "",
    @SerialName("name") val name: String = "",
    @SerialName("desc") val desc: List<String> = emptyList(),
    @SerialName("equipment_category") val equipmentCategory: ApiReferenceDto = ApiReferenceDto(),
    @SerialName("rarity") val rarity: MagicItemRarityDto = MagicItemRarityDto(),
    @SerialName("variants") val variants: List<ApiReferenceDto> = emptyList(),
    @SerialName("variant") val variant: Boolean = false,
    @SerialName("image") val image: String? = null,
    @SerialName("url") val url: String = "",
)

/**
 * Rarity name values: "Varies" | "Common" | "Uncommon" | "Rare" | "Very Rare" | "Legendary" | "Artifact"
 */
@Serializable
data class MagicItemRarityDto(
    @SerialName("name") val name: String = "Common",
)
