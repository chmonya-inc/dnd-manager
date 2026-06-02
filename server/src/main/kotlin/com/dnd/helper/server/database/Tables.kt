package com.dnd.helper.server.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json
import kotlinx.serialization.json.Json

val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
    encodeDefaults = true
}

object Characters : Table("characters") {
    val id = varchar("id", 50)
    val sessionId = varchar("session_id", 50)
    val name = varchar("name", 100)
    val playerName = varchar("player_name", 100)
    val race = varchar("race", 50)
    val characterClass = varchar("character_class", 50)
    val level = integer("level")
    val description = text("description")
    val imageUrl = varchar("image_url", 500).nullable()
    val maxHp = integer("max_hp")
    val currentHp = integer("current_hp")
    val strength = integer("strength")
    val dexterity = integer("dexterity")
    val constitution = integer("constitution")
    val intelligence = integer("intelligence")
    val wisdom = integer("wisdom")
    val charisma = integer("charisma")
    val subclass = varchar("subclass", 100)
    val background = varchar("background", 100)
    val experiencePoints = integer("experience_points")
    
    // Complex objects as JSON
    val appearance = json<com.dnd.helper.domain.model.CharacterAppearance>("appearance", json)
    val combat = json<com.dnd.helper.domain.model.CharacterCombat>("combat", json)
    val proficiencies = json<com.dnd.helper.domain.model.CharacterProficiencies>("proficiencies", json)
    val weapons = json<List<com.dnd.helper.domain.model.Weapon>>("weapons", json)
    val features = json<com.dnd.helper.domain.model.CharacterFeatures>("features", json)
    val skills = json<List<com.dnd.helper.domain.model.Skill>>("skills", json)
    val items = json<List<com.dnd.helper.domain.model.Item>>("items", json)
    val notes = json<List<com.dnd.helper.domain.model.Note>>("notes", json)

    override val primaryKey = PrimaryKey(id, sessionId)
}

object Locations : Table("locations") {
    val id = varchar("id", 50)
    val sessionId = varchar("session_id", 50)
    val name = varchar("name", 100)
    val description = text("description")
    val imageUrl = varchar("image_url", 500).nullable()
    override val primaryKey = PrimaryKey(id, sessionId)
}

object Battlefields : Table("battlefields") {
    val id = varchar("id", 50)
    val sessionId = varchar("session_id", 50)
    val name = varchar("name", 100)
    val description = text("description")
    val imageUrl = varchar("image_url", 500).nullable()
    override val primaryKey = PrimaryKey(id, sessionId)
}

object Monsters : Table("monsters") {
    val id = varchar("id", 50)
    val sessionId = varchar("session_id", 50)
    val name = varchar("name", 100)
    val description = text("description")
    val imageUrl = varchar("image_url", 500).nullable()
    val stats = json<com.dnd.helper.domain.model.CharacterStats>("stats", json)
    val maxHp = integer("max_hp")
    val currentHp = integer("current_hp")
    val armorClass = integer("armor_class")
    val speed = integer("speed")
    val challengeRating = varchar("challenge_rating", 10)
    val type = varchar("type", 50)
    val alignment = varchar("alignment", 50)
    val size = varchar("size", 20)
    override val primaryKey = PrimaryKey(id, sessionId)
}

object Npcs : Table("npcs") {
    val id = varchar("id", 50)
    val sessionId = varchar("session_id", 50)
    val name = varchar("name", 100)
    val description = text("description")
    val imageUrl = varchar("image_url", 500).nullable()
    val background = text("background")
    override val primaryKey = PrimaryKey(id, sessionId)
}

object Music : Table("music") {
    val id = varchar("id", 50)
    val sessionId = varchar("session_id", 50)
    val name = varchar("name", 100)
    val url = varchar("url", 500)
    override val primaryKey = PrimaryKey(id, sessionId)
}

object Events : Table("events") {
    val id = varchar("id", 50)
    val sessionId = varchar("session_id", 50)
    val name = varchar("name", 100)
    val items = json<List<com.dnd.helper.domain.model.PresentedItem>>("items", json)
    override val primaryKey = PrimaryKey(id, sessionId)
}

object Logs : Table("logs") {
    val sessionId = varchar("session_id", 50)
    val timestamp = varchar("timestamp", 50)
    val action = varchar("action", 100)
    val details = text("details")
    val initialState = text("initial_state")
    val endState = text("end_state")
    val success = bool("success")
}
