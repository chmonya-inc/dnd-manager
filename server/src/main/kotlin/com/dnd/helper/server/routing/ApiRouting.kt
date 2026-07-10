package com.dnd.helper.server.routing

import com.dnd.helper.domain.model.Battlefield
import com.dnd.helper.domain.model.Character
import com.dnd.helper.domain.model.CharacterStats
import com.dnd.helper.domain.model.GameEvent
import com.dnd.helper.domain.model.InitialData
import com.dnd.helper.domain.model.Location
import com.dnd.helper.domain.model.LogEntry
import com.dnd.helper.domain.model.Monster
import com.dnd.helper.domain.model.MusicTrack
import com.dnd.helper.domain.model.Npc
import com.dnd.helper.server.database.Battlefields
import com.dnd.helper.server.database.Campaigns
import com.dnd.helper.server.database.Characters
import com.dnd.helper.server.database.DatabaseFactory.dbQuery
import com.dnd.helper.server.database.Events
import com.dnd.helper.server.database.Locations
import com.dnd.helper.server.database.Logs
import com.dnd.helper.server.database.Monsters
import com.dnd.helper.server.database.Music
import com.dnd.helper.server.database.Npcs
import com.dnd.helper.server.database.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.upsert

fun Route.configureApiRouting() {
    authenticate("auth-jwt") {
        route("/api/{sessionId}") {
            webSocket("/ws") {
                val sessionId = call.parameters["sessionId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session ID"))
                if (!ensureSessionAccess(call, sessionId, respondOnError = false)) {
                    return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Access denied"))
                }
                SessionManager.addClient(sessionId, this)
                try {
                    @Suppress("UnusedPrivateProperty")
                    for (frame in incoming) {
                        // Keeping connection alive
                    }
                } finally {
                    SessionManager.removeClient(sessionId, this)
                }
            }

            get("/initial-data") {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                if (!ensureSessionAccess(call, sessionId)) return@get
                call.respond(handleGetInitialData(sessionId))
            }

            route("/characters") {
                get {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    call.respond(handleGetCharacters(sessionId))
                }
                get("/{id}") {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val char = handleGetCharacter(id, sessionId)
                    if (char == null) call.respond(HttpStatusCode.NotFound) else call.respond(char)
                }
                post {
                    val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@post
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()

                    val userRole = transaction {
                        Users.selectAll().where { Users.id eq (userId ?: "") }.singleOrNull()?.get(Users.role)
                    }

                    val char = call.receive<Character>()
                    // Only pass userId if the user is a PLAYER, so MASTER doesn't overwrite ownership
                    val ownershipId = if (userRole == "PLAYER") userId else null
                    handleSaveCharacter(char, sessionId, ownershipId)
                    SessionManager.notifyUpdate(sessionId, "characters", char.id)
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@delete
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (!ensureCharacterOwnership(call, id)) return@delete
                    handleDeleteCharacter(id, sessionId)
                    SessionManager.notifyUpdate(sessionId, "characters", id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/locations") {
                get {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    call.respond(handleGetLocations(sessionId))
                }
                post {
                    val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@post
                    val loc = call.receive<Location>()
                    handleSaveLocation(loc, sessionId)
                    SessionManager.notifyUpdate(sessionId, "locations", loc.id)
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@delete
                    if (!ensureMasterRole(call)) return@delete
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    handleDeleteLocation(id, sessionId)
                    SessionManager.notifyUpdate(sessionId, "locations", id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/battlefields") {
                get {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    call.respond(handleGetBattlefields(sessionId))
                }
                post {
                    val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@post
                    val bf = call.receive<Battlefield>()
                    handleSaveBattlefield(bf, sessionId)
                    SessionManager.notifyUpdate(sessionId, "battlefields", bf.id)
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@delete
                    if (!ensureMasterRole(call)) return@delete
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    handleDeleteBattlefield(id, sessionId)
                    SessionManager.notifyUpdate(sessionId, "battlefields", id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/monsters") {
                get {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    call.respond(handleGetMonsters(sessionId))
                }
                post {
                    val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@post
                    val monster = call.receive<Monster>()
                    handleSaveMonster(monster, sessionId)
                    SessionManager.notifyUpdate(sessionId, "monsters", monster.id)
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@delete
                    if (!ensureMasterRole(call)) return@delete
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    handleDeleteMonster(id, sessionId)
                    SessionManager.notifyUpdate(sessionId, "monsters", id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/npcs") {
                get {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    call.respond(handleGetNpcs(sessionId))
                }
                post {
                    val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@post
                    val npc = call.receive<Npc>()
                    handleSaveNpc(npc, sessionId)
                    SessionManager.notifyUpdate(sessionId, "npcs", npc.id)
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@delete
                    if (!ensureMasterRole(call)) return@delete
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    handleDeleteNpc(id, sessionId)
                    SessionManager.notifyUpdate(sessionId, "npcs", id)
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/music") {
                get {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    call.respond(handleGetMusic(sessionId))
                }
                post {
                    val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@post
                    val track = call.receive<MusicTrack>()
                    handleSaveMusic(track, sessionId)
                    SessionManager.notifyUpdate(sessionId, "music")
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@delete
                    if (!ensureMasterRole(call)) return@delete
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    handleDeleteMusic(id, sessionId)
                    SessionManager.notifyUpdate(sessionId, "music")
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/logs") {
                get {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    call.respond(handleGetLogs(sessionId))
                }
                post {
                    val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@post
                    val log = call.receive<LogEntry>()
                    handleSaveLog(log, sessionId)
                    SessionManager.notifyUpdate(sessionId, "logs")
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/events") {
                get {
                    val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@get
                    call.respond(handleGetEvents(sessionId))
                }
                post {
                    val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@post
                    val event = call.receive<GameEvent>()
                    handleSaveEvent(event, sessionId)
                    SessionManager.notifyUpdate(sessionId, "events")
                    call.respond(HttpStatusCode.OK)
                }
                delete("/{id}") {
                    val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    if (!ensureSessionAccess(call, sessionId)) return@delete
                    if (!ensureMasterRole(call)) return@delete
                    val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                    handleDeleteEvent(id, sessionId)
                    SessionManager.notifyUpdate(sessionId, "events")
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}

private suspend fun ensureSessionAccess(call: ApplicationCall, sessionId: String, respondOnError: Boolean = true): Boolean {
    val principal = call.principal<JWTPrincipal>()
    val userId = principal?.payload?.getClaim("userId")?.asString() ?: return false

    val user = dbQuery {
        Users.selectAll().where { Users.id eq userId }.singleOrNull()
    } ?: return false

    val userRole = user[Users.role]

    if (userRole == "MASTER") {
        // Masters MUST own the campaign associated with this sessionId
        val campaign = dbQuery {
            Campaigns.selectAll().where { (Campaigns.sessionId eq sessionId) and (Campaigns.ownerId eq userId) }.singleOrNull()
        }
        if (campaign == null) {
            if (respondOnError) {
                call.respond(HttpStatusCode.Forbidden, "You do not own this campaign")
            }
            return false
        }
    } else {
        // Players are allowed to access if they have the sessionId
        // (This matches current app behavior where players join by ID)
    }

    return true
}

private suspend fun ensureMasterRole(call: ApplicationCall): Boolean {
    val principal = call.principal<JWTPrincipal>()
    val userId = principal?.payload?.getClaim("userId")?.asString() ?: return false

    val userRole = dbQuery {
        Users.selectAll().where { Users.id eq userId }.singleOrNull()?.get(Users.role)
    }

    if (userRole != "MASTER") {
        call.respond(HttpStatusCode.Forbidden, "Only a Master can perform this action")
        return false
    }
    return true
}

private suspend fun ensureCharacterOwnership(call: ApplicationCall, characterId: String): Boolean {
    val principal = call.principal<JWTPrincipal>()
    val userId = principal?.payload?.getClaim("userId")?.asString() ?: return false

    val userRole = dbQuery {
        Users.selectAll().where { Users.id eq userId }.singleOrNull()?.get(Users.role)
    }
    if (userRole == "MASTER") return true // Masters can delete any character in their session

    val charOwnerId = dbQuery {
        Characters.selectAll().where { Characters.id eq characterId }.singleOrNull()?.get(Characters.userId)
    }
    if (charOwnerId != null && charOwnerId != userId) {
        call.respond(HttpStatusCode.Forbidden, "You do not own this character")
        return false
    }
    return true
}

private suspend fun handleGetInitialData(sessionId: String): InitialData {
    return InitialData(
        characters = handleGetCharacters(sessionId),
        locations = handleGetLocations(sessionId),
        battlefields = handleGetBattlefields(sessionId),
        monsters = handleGetMonsters(sessionId),
        npcs = handleGetNpcs(sessionId),
        music = handleGetMusic(sessionId),
        events = handleGetEvents(sessionId),
        lastModified = ""
    )
}

private suspend fun handleGetCharacters(sessionId: String): List<Character> = dbQuery {
    Characters.leftJoin(Users, { Characters.userId }, { Users.id })
        .selectAll().where { Characters.sessionId eq sessionId }.map { rowToCharacter(it) }
}

private suspend fun handleGetCharacter(id: String?, sessionId: String): Character? = dbQuery {
    if (id == null) return@dbQuery null
    Characters.leftJoin(Users, { Characters.userId }, { Users.id })
        .selectAll().where {
            (Characters.id eq id) and (Characters.sessionId eq sessionId)
        }.singleOrNull()?.let {
            rowToCharacter(
                it
            )
        }
}

private suspend fun handleSaveCharacter(char: Character?, sessionId: String, userId: String? = null) = dbQuery {
    if (char == null) return@dbQuery
    Characters.upsert {
        it[id] = char.id
        it[Characters.sessionId] = sessionId
        if (userId != null) {
            it[Characters.userId] = userId
        }
        it[name] = char.name
        it[playerName] = char.playerName
        it[race] = char.race
        it[characterClass] = char.characterClass
        it[level] = char.level
        it[description] = char.description
        it[imageUrl] = char.imageUrl
        it[maxHp] = char.maxHp
        it[currentHp] = char.currentHp
        it[strength] = char.stats.strength
        it[dexterity] = char.stats.dexterity
        it[constitution] = char.stats.constitution
        it[intelligence] = char.stats.intelligence
        it[wisdom] = char.stats.wisdom
        it[charisma] = char.stats.charisma
        it[subclass] = char.subclass
        it[background] = char.background
        it[experiencePoints] = char.experiencePoints
        it[appearance] = char.appearance
        it[combat] = char.combat
        it[proficiencies] = char.proficiencies
        it[weapons] = char.weapons
        it[features] = char.features
        it[spells] = char.spells
        it[items] = char.items
        it[notes] = char.notes
    }
}

private suspend fun handleDeleteCharacter(id: String?, sessionId: String) = dbQuery {
    if (id == null) return@dbQuery
    Characters.deleteWhere { (Characters.id eq id) and (Characters.sessionId eq sessionId) }
}

private suspend fun handleGetLocations(sessionId: String): List<Location> = dbQuery {
    Locations.selectAll().where { Locations.sessionId eq sessionId }.map {
        Location(it[Locations.id], it[Locations.name], it[Locations.description], it[Locations.imageUrl])
    }
}

private suspend fun handleSaveLocation(loc: Location?, sessionId: String) = dbQuery {
    if (loc == null) return@dbQuery
    Locations.upsert {
        it[id] = loc.id
        it[Locations.sessionId] = sessionId
        it[name] = loc.name
        it[description] = loc.description
        it[imageUrl] = loc.imageUrl
    }
}

private suspend fun handleDeleteLocation(id: String?, sessionId: String) = dbQuery {
    if (id == null) return@dbQuery
    Locations.deleteWhere { (Locations.id eq id) and (Locations.sessionId eq sessionId) }
}

private suspend fun handleGetBattlefields(sessionId: String): List<Battlefield> = dbQuery {
    Battlefields.selectAll().where { Battlefields.sessionId eq sessionId }.map {
        Battlefield(it[Battlefields.id], it[Battlefields.name], it[Battlefields.description], it[Battlefields.imageUrl])
    }
}

private suspend fun handleSaveBattlefield(bf: Battlefield?, sessionId: String) = dbQuery {
    if (bf == null) return@dbQuery
    Battlefields.upsert {
        it[id] = bf.id
        it[Battlefields.sessionId] = sessionId
        it[name] = bf.name
        it[description] = bf.description
        it[imageUrl] = bf.imageUrl
    }
}

private suspend fun handleDeleteBattlefield(id: String?, sessionId: String) = dbQuery {
    if (id == null) return@dbQuery
    Battlefields.deleteWhere { (Battlefields.id eq id) and (Battlefields.sessionId eq sessionId) }
}

private suspend fun handleGetMonsters(sessionId: String): List<Monster> = dbQuery {
    Monsters.selectAll().where { Monsters.sessionId eq sessionId }.map {
        Monster(
            id = it[Monsters.id],
            name = it[Monsters.name],
            description = it[Monsters.description],
            imageUrl = it[Monsters.imageUrl],
            stats = it[Monsters.stats],
            maxHp = it[Monsters.maxHp],
            currentHp = it[Monsters.currentHp],
            armorClass = it[Monsters.armorClass],
            armorClassDetails = it[Monsters.armorClassDetails],
            hitDice = it[Monsters.hitDice],
            speed = it[Monsters.speed],
            speedDetails = it[Monsters.speedDetails],
            challengeRating = it[Monsters.challengeRating],
            type = it[Monsters.type],
            alignment = it[Monsters.alignment],
            size = it[Monsters.size],
            proficiencies = it[Monsters.proficiencies],
            conditionImmunities = it[Monsters.conditionImmunities],
            damageImmunities = it[Monsters.damageImmunities],
            damageResistances = it[Monsters.damageResistances],
            damageVulnerabilities = it[Monsters.damageVulnerabilities],
            languages = it[Monsters.languages],
            specialAbilities = it[Monsters.specialAbilities],
            actions = it[Monsters.actions],
            legendaryActions = it[Monsters.legendaryActions],
            reactions = it[Monsters.reactions]
        )
    }
}

private suspend fun handleSaveMonster(m: Monster?, sessionId: String) = dbQuery {
    if (m == null) return@dbQuery
    Monsters.upsert {
        it[id] = m.id
        it[Monsters.sessionId] = sessionId
        it[name] = m.name
        it[description] = m.description
        it[imageUrl] = m.imageUrl
        it[stats] = m.stats
        it[maxHp] = m.maxHp
        it[currentHp] = m.currentHp
        it[armorClass] = m.armorClass
        it[armorClassDetails] = m.armorClassDetails
        it[hitDice] = m.hitDice
        it[speed] = m.speed
        it[speedDetails] = m.speedDetails
        it[challengeRating] = m.challengeRating
        it[type] = m.type
        it[alignment] = m.alignment
        it[size] = m.size
        it[proficiencies] = m.proficiencies
        it[conditionImmunities] = m.conditionImmunities
        it[damageImmunities] = m.damageImmunities
        it[damageResistances] = m.damageResistances
        it[damageVulnerabilities] = m.damageVulnerabilities
        it[languages] = m.languages
        it[specialAbilities] = m.specialAbilities
        it[actions] = m.actions
        it[legendaryActions] = m.legendaryActions
        it[reactions] = m.reactions
    }
}

private suspend fun handleDeleteMonster(id: String?, sessionId: String) = dbQuery {
    if (id == null) return@dbQuery
    Monsters.deleteWhere { (Monsters.id eq id) and (Monsters.sessionId eq sessionId) }
}

private suspend fun handleGetNpcs(sessionId: String): List<Npc> = dbQuery {
    Npcs.selectAll().where { Npcs.sessionId eq sessionId }.map {
        Npc(it[Npcs.id], it[Npcs.name], it[Npcs.description], it[Npcs.imageUrl], it[Npcs.background])
    }
}

private suspend fun handleSaveNpc(n: Npc?, sessionId: String) = dbQuery {
    if (n == null) return@dbQuery
    Npcs.upsert {
        it[id] = n.id
        it[Npcs.sessionId] = sessionId
        it[name] = n.name
        it[description] = n.description
        it[imageUrl] = n.imageUrl
        it[background] = n.background
    }
}

private suspend fun handleDeleteNpc(id: String?, sessionId: String) = dbQuery {
    if (id == null) return@dbQuery
    Npcs.deleteWhere { (Npcs.id eq id) and (Npcs.sessionId eq sessionId) }
}

private suspend fun handleGetMusic(sessionId: String): List<MusicTrack> = dbQuery {
    Music.selectAll().where { Music.sessionId eq sessionId }.map {
        MusicTrack(
            it[Music.id],
            it[Music.name],
            it[Music.url]
        )
    }
}

private suspend fun handleSaveMusic(m: MusicTrack?, sessionId: String) = dbQuery {
    if (m == null) return@dbQuery
    Music.upsert {
        it[id] = m.id
        it[Music.sessionId] = sessionId
        it[name] = m.name
        it[url] = m.url
    }
}

private suspend fun handleDeleteMusic(id: String?, sessionId: String) = dbQuery {
    if (id == null) return@dbQuery
    Music.deleteWhere { (Music.id eq id) and (Music.sessionId eq sessionId) }
}

private suspend fun handleGetLogs(sessionId: String): List<LogEntry> = dbQuery {
    Logs.selectAll().where { Logs.sessionId eq sessionId }.orderBy(Logs.timestamp, SortOrder.DESC).limit(100).map {
        LogEntry(
            timestamp = it[Logs.timestamp],
            action = it[Logs.action],
            details = it[Logs.details],
            initialState = it[Logs.initialState],
            endState = it[Logs.endState],
            success = it[Logs.success]
        )
    }
}

private suspend fun handleSaveLog(log: LogEntry?, sessionId: String) = dbQuery {
    if (log == null) return@dbQuery
    Logs.insert {
        it[Logs.sessionId] = sessionId
        it[timestamp] = log.timestamp ?: java.time.Instant.now().toString()
        it[action] = log.action
        it[details] = log.details ?: ""
        it[initialState] = log.initialState?.toString() ?: ""
        it[endState] = log.endState?.toString() ?: ""
        it[success] = log.success
    }
}

private suspend fun handleGetEvents(sessionId: String): List<GameEvent> = dbQuery {
    Events.selectAll().where { Events.sessionId eq sessionId }.map {
        GameEvent(
            it[Events.id],
            it[Events.name],
            it[Events.items]
        )
    }
}

private suspend fun handleSaveEvent(e: GameEvent?, sessionId: String) = dbQuery {
    if (e == null) return@dbQuery
    Events.upsert {
        it[id] = e.id
        it[Events.sessionId] = sessionId
        it[name] = e.name
        it[items] = e.items
    }
}

private suspend fun handleDeleteEvent(id: String?, sessionId: String) = dbQuery {
    if (id == null) return@dbQuery
    Events.deleteWhere { (Events.id eq id) and (Events.sessionId eq sessionId) }
}

fun rowToCharacter(row: ResultRow): Character {
    return Character(
        id = row[Characters.id],
        name = row[Characters.name],
        playerName = row[Characters.playerName],
        ownerUserId = row[Characters.userId],
        ownerUsername = try { row[Users.username] } catch (_: Exception) { null },
        race = row[Characters.race],
        characterClass = row[Characters.characterClass],
        level = row[Characters.level],
        description = row[Characters.description],
        imageUrl = row[Characters.imageUrl],
        maxHp = row[Characters.maxHp],
        currentHp = row[Characters.currentHp],
        stats = CharacterStats(
            strength = row[Characters.strength],
            dexterity = row[Characters.dexterity],
            constitution = row[Characters.constitution],
            intelligence = row[Characters.intelligence],
            wisdom = row[Characters.wisdom],
            charisma = row[Characters.charisma]
        ),
        subclass = row[Characters.subclass],
        background = row[Characters.background],
        experiencePoints = row[Characters.experiencePoints],
        appearance = row[Characters.appearance],
        combat = row[Characters.combat],
        proficiencies = row[Characters.proficiencies],
        weapons = row[Characters.weapons],
        features = row[Characters.features],
        spells = row[Characters.spells],
        items = row[Characters.items],
        notes = row[Characters.notes]
    )
}
