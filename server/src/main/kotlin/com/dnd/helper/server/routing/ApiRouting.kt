package com.dnd.helper.server.routing

import com.dnd.helper.domain.model.*
import com.dnd.helper.server.database.*
import com.dnd.helper.server.database.DatabaseFactory.dbQuery
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

fun Route.configureApiRouting() {
    route("/api/{sessionId}") {
        webSocket("/ws") {
            val sessionId = call.parameters["sessionId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session ID"))
            SessionManager.addClient(sessionId, this)
            try {
                for (frame in incoming) {
                    // Keeping connection alive
                }
            } finally {
                SessionManager.removeClient(sessionId, this)
            }
        }

        get("/initial-data") {
            val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(handleGetInitialData(sessionId))
        }

        route("/characters") {
            get {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(handleGetCharacters(sessionId))
            }
            get("/{id}") {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val char = handleGetCharacter(id, sessionId)
                if (char == null) call.respond(HttpStatusCode.NotFound) else call.respond(char)
            }
            post {
                val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val char = call.receive<Character>()
                handleSaveCharacter(char, sessionId)
                SessionManager.notifyUpdate(sessionId, "characters")
                call.respond(HttpStatusCode.OK)
            }
            delete("/{id}") {
                val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                handleDeleteCharacter(id, sessionId)
                SessionManager.notifyUpdate(sessionId, "characters")
                call.respond(HttpStatusCode.OK)
            }
        }

        route("/locations") {
            get {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(handleGetLocations(sessionId))
            }
            post {
                val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val loc = call.receive<Location>()
                handleSaveLocation(loc, sessionId)
                SessionManager.notifyUpdate(sessionId, "locations")
                call.respond(HttpStatusCode.OK)
            }
            delete("/{id}") {
                val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                handleDeleteLocation(id, sessionId)
                SessionManager.notifyUpdate(sessionId, "locations")
                call.respond(HttpStatusCode.OK)
            }
        }

        route("/monsters") {
            get {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(handleGetMonsters(sessionId))
            }
            post {
                val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val monster = call.receive<Monster>()
                handleSaveMonster(monster, sessionId)
                SessionManager.notifyUpdate(sessionId, "monsters")
                call.respond(HttpStatusCode.OK)
            }
            delete("/{id}") {
                val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                handleDeleteMonster(id, sessionId)
                SessionManager.notifyUpdate(sessionId, "monsters")
                call.respond(HttpStatusCode.OK)
            }
        }

        route("/npcs") {
            get {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(handleGetNpcs(sessionId))
            }
            post {
                val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val npc = call.receive<Npc>()
                handleSaveNpc(npc, sessionId)
                SessionManager.notifyUpdate(sessionId, "npcs")
                call.respond(HttpStatusCode.OK)
            }
            delete("/{id}") {
                val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                handleDeleteNpc(id, sessionId)
                SessionManager.notifyUpdate(sessionId, "npcs")
                call.respond(HttpStatusCode.OK)
            }
        }

        route("/music") {
            get {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(handleGetMusic(sessionId))
            }
            post {
                val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val track = call.receive<MusicTrack>()
                handleSaveMusic(track, sessionId)
                SessionManager.notifyUpdate(sessionId, "music")
                call.respond(HttpStatusCode.OK)
            }
            delete("/{id}") {
                val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                handleDeleteMusic(id, sessionId)
                SessionManager.notifyUpdate(sessionId, "music")
                call.respond(HttpStatusCode.OK)
            }
        }

        route("/logs") {
            get {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(handleGetLogs(sessionId))
            }
            post {
                val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val log = call.receive<LogEntry>()
                handleSaveLog(log, sessionId)
                SessionManager.notifyUpdate(sessionId, "logs")
                call.respond(HttpStatusCode.OK)
            }
        }

        route("/events") {
            get {
                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                call.respond(handleGetEvents(sessionId))
            }
            post {
                val sessionId = call.parameters["sessionId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val event = call.receive<GameEvent>()
                handleSaveEvent(event, sessionId)
                SessionManager.notifyUpdate(sessionId, "events")
                call.respond(HttpStatusCode.OK)
            }
            delete("/{id}") {
                val sessionId = call.parameters["sessionId"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                handleDeleteEvent(id, sessionId)
                SessionManager.notifyUpdate(sessionId, "events")
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private suspend fun handleGetInitialData(sessionId: String): InitialData {
    return InitialData(
        characters = handleGetCharacters(sessionId),
        locations = handleGetLocations(sessionId),
        monsters = handleGetMonsters(sessionId),
        npcs = handleGetNpcs(sessionId),
        music = handleGetMusic(sessionId),
        events = handleGetEvents(sessionId),
        lastModified = "" 
    )
}

private suspend fun handleGetCharacters(sessionId: String): List<Character> = dbQuery {
    Characters.selectAll().where { Characters.sessionId eq sessionId }.map { rowToCharacter(it) }
}

private suspend fun handleGetCharacter(id: String?, sessionId: String): Character? = dbQuery {
    if (id == null) return@dbQuery null
    Characters.selectAll().where { (Characters.id eq id) and (Characters.sessionId eq sessionId) }.singleOrNull()?.let { rowToCharacter(it) }
}

private suspend fun handleSaveCharacter(char: Character?, sessionId: String) = dbQuery {
    if (char == null) return@dbQuery
    Characters.upsert {
        it[id] = char.id
        it[Characters.sessionId] = sessionId
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
        it[skills] = char.skills
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
            speed = it[Monsters.speed],
            challengeRating = it[Monsters.challengeRating],
            type = it[Monsters.type],
            alignment = it[Monsters.alignment],
            size = it[Monsters.size]
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
        it[speed] = m.speed
        it[challengeRating] = m.challengeRating
        it[type] = m.type
        it[alignment] = m.alignment
        it[size] = m.size
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
    Music.selectAll().where { Music.sessionId eq sessionId }.map { MusicTrack(it[Music.id], it[Music.name], it[Music.url]) }
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
    Events.selectAll().where { Events.sessionId eq sessionId }.map { GameEvent(it[Events.id], it[Events.name], it[Events.items]) }
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

private fun rowToCharacter(row: ResultRow): Character {
    return Character(
        id = row[Characters.id],
        name = row[Characters.name],
        playerName = row[Characters.playerName],
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
        skills = row[Characters.skills],
        items = row[Characters.items],
        notes = row[Characters.notes]
    )
}
