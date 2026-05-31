package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterFeatures(
    val classFeatures: List<String> = emptyList(),
    val racialTraits: List<String> = emptyList(),
    val feats: List<String> = emptyList(),
)
