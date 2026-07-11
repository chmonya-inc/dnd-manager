package com.dnd.helper.server.database

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
    encodeDefaults = true
}

object RefreshTokens : Table("refresh_tokens") {
    val tokenHash = varchar("token_hash", 64) // SHA-256 hex of the raw JWT
    val userId = varchar("user_id", 50)
    val expiresAt = long("expires_at") // unix millis
    override val primaryKey = PrimaryKey(tokenHash)
}

object Users : Table("users") {
    val id = varchar("id", 50)
    val username = varchar("username", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val recoverCodeHash = varchar("recover_code_hash", 255).nullable()
    val role = varchar("role", 20).default("PLAYER") // MASTER or PLAYER
    override val primaryKey = PrimaryKey(id)
}

object Campaigns : Table("campaigns") {
    val id = varchar("id", 50)
    val name = varchar("name", 200)
    val ownerId = varchar("owner_id", 50) // references Users.id (the Master)
    val sessionId = varchar("session_id", 50) // links to the existing session/table concept
    val isStarted = bool("is_started").default(false)
    override val primaryKey = PrimaryKey(id)
}

object Characters : Table("characters") {
    val id = varchar("id", 50)
    val sessionId = varchar("session_id", 50)
    val userId = varchar("user_id", 50).nullable()
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
    val appearance = json<com.dnd.helper.domain.model.CharacterAppearance>(
        "appearance",
        json
    ).default(com.dnd.helper.domain.model.CharacterAppearance())
    val combat = json<com.dnd.helper.domain.model.CharacterCombat>(
        "combat",
        json
    ).default(com.dnd.helper.domain.model.CharacterCombat())
    val proficiencies = json<com.dnd.helper.domain.model.CharacterProficiencies>(
        "proficiencies",
        json
    ).default(com.dnd.helper.domain.model.CharacterProficiencies())
    val weapons = json<List<com.dnd.helper.domain.model.Weapon>>("weapons", json).default(emptyList())
    val features = json<com.dnd.helper.domain.model.CharacterFeatures>(
        "features",
        json
    ).default(com.dnd.helper.domain.model.CharacterFeatures())
    val spells = json<List<com.dnd.helper.domain.model.Spell>>("spells", json).default(emptyList())
    val items = json<List<com.dnd.helper.domain.model.Item>>("items", json).default(emptyList())
    val notes = json<List<com.dnd.helper.domain.model.Note>>("notes", json).default(emptyList())

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
    val armorClassDetails = json<List<com.dnd.helper.data.remote.dto.monster.MonsterArmorClassDto>>(
        "armor_class_details",
        json
    ).default(emptyList())
    val hitDice = varchar("hit_dice", 50).default("")
    val speed = integer("speed")
    val speedDetails = json<com.dnd.helper.data.remote.dto.monster.MonsterSpeedDto>(
        "speed_details",
        json
    ).default(com.dnd.helper.data.remote.dto.monster.MonsterSpeedDto())
    val challengeRating = varchar("challenge_rating", 10)
    val type = varchar("type", 50)
    val alignment = varchar("alignment", 50)
    val size = varchar("size", 20)
    val proficiencies = json<List<com.dnd.helper.data.remote.dto.monster.MonsterProficiencyDto>>(
        "proficiencies",
        json
    ).default(emptyList())
    val conditionImmunities = json<List<String>>("condition_immunities", json).default(emptyList())
    val damageImmunities = json<List<String>>("damage_immunities", json).default(emptyList())
    val damageResistances = json<List<String>>("damage_resistances", json).default(emptyList())
    val damageVulnerabilities = json<List<String>>("damage_vulnerabilities", json).default(emptyList())
    val languages = json<List<String>>("languages", json).default(emptyList())
    val specialAbilities = json<List<com.dnd.helper.data.remote.dto.monster.MonsterSpecialAbilityDto>>(
        "special_abilities",
        json
    ).default(emptyList())
    val actions = json<List<com.dnd.helper.data.remote.dto.monster.MonsterActionDto>>(
        "actions",
        json
    ).default(emptyList())
    val legendaryActions = json<List<com.dnd.helper.data.remote.dto.monster.MonsterActionDto>>(
        "legendary_actions",
        json
    ).default(emptyList())
    val reactions = json<List<com.dnd.helper.data.remote.dto.monster.MonsterActionDto>>(
        "reactions",
        json
    ).default(emptyList())
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

/**
 * Tracks character assignment requests from Master to Player.
 * - PENDING:   Player hasn't responded yet
 * - ACCEPTED:  Player accepted; character.userId is set to the player
 * - REVOKED:   Player rejected; character is removed from player visibility
 */
object CharacterAssignments : Table("character_assignments") {
    val id = varchar("id", 50)
    val characterId = varchar("character_id", 50)
    val sessionId = varchar("session_id", 50)
    val masterId = varchar("master_id", 50)
    val playerId = varchar("player_id", 50)
    val status = varchar("status", 20).default("PENDING") // PENDING, ACCEPTED, REVOKED
    val createdAt = long("created_at")
    val respondedAt = long("responded_at").nullable()
    override val primaryKey = PrimaryKey(id)
}
