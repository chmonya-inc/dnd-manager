package com.dnd.helper.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class InitialData(
    val characters: List<Character>,
    val locations: List<Location>,
    val monsters: List<Monster>,
    val npcs: List<Npc>,
    val lastModified: String
)
