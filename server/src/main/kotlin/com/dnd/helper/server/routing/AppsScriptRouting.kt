package com.dnd.helper.server.routing

import com.dnd.helper.data.remote.AppsScriptRequest
import com.dnd.helper.data.remote.AppsScriptResponse
import com.dnd.helper.domain.model.*
import com.dnd.helper.server.database.*
import com.dnd.helper.server.database.DatabaseFactory.dbQuery
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant

fun Route.configureAppsScriptRouting() {
    route("/exec") {
        get {
            val requestJson = call.parameters["request"]
            if (requestJson == null) {
                call.respond(AppsScriptResponse<Unit>(success = false, error = "Missing request"))
                return@get
            }
            val request = Json.decodeFromString<AppsScriptRequest>(requestJson)
            handleLegacyRequest(request, call)
        }
        post {
            val request = call.receive<AppsScriptRequest>()
            handleLegacyRequest(request, call)
        }
    }
}

private suspend fun handleLegacyRequest(request: AppsScriptRequest, call: ApplicationCall) {
    try {
        val result: Any? = when (request.action) {
            "getInitialData" -> handleGetInitialData()
            "getCharacters" -> handleGetCharacters()
            "getCharacter" -> handleGetCharacter(request.id)
            "saveCharacter" -> { handleSaveCharacter(request.character); Unit }
            "deleteCharacter" -> { handleDeleteCharacter(request.id); Unit }
            "getLastModified" -> handleGetLastModified()
            "getLocations" -> handleGetLocations()
            "saveLocation" -> { handleSaveLocation(request.location); Unit }
            "deleteLocation" -> { handleDeleteLocation(request.id); Unit }
            "getMonsters" -> handleGetMonsters()
            "saveMonster" -> { handleSaveMonster(request.monster); Unit }
            "deleteMonster" -> { handleDeleteMonster(request.id); Unit }
            "getNpcs" -> handleGetNpcs()
            "saveNpc" -> { handleSaveNpc(request.npc); Unit }
            "deleteNpc" -> { handleDeleteNpc(request.id); Unit }
            "getMusic" -> handleGetMusic()
            "saveMusic" -> { handleSaveMusic(request.music); Unit }
            "deleteMusic" -> { handleDeleteMusic(request.id); Unit }
            "getLogs" -> handleGetLogs()
            "saveLog" -> { handleSaveLog(request.log); Unit }
            "getEvents" -> handleGetEvents()
            "saveEvent" -> { handleSaveEvent(request.event); Unit }
            "deleteEvent" -> { handleDeleteEvent(request.id); Unit }
            else -> {
                call.respond(AppsScriptResponse<Unit>(success = false, error = "Unknown action: ${request.action}"))
                return
            }
        }
        
        if (result is AppsScriptResponse<*>) {
            call.respond(result)
        } else {
            call.respond(AppsScriptResponse(success = true, data = result))
        }
    } catch (e: Exception) {
        call.respond(AppsScriptResponse<Unit>(success = false, error = e.message))
    }
}

private suspend fun updateLastModified() = dbQuery {
    val now = Instant.now().toString()
    Metadata.upsert {
        it[key] = "lastModified"
        it[value] = now
    }
}

private suspend fun handleGetInitialData(): InitialData {
    return InitialData(
        characters = handleGetCharacters(),
        locations = handleGetLocations(),
        monsters = handleGetMonsters(),
        npcs = handleGetNpcs(),
        music = handleGetMusic(),
        events = handleGetEvents(),
        lastModified = handleGetLastModified()
    )
}

private suspend fun handleGetCharacters(): List<Character> = dbQuery {
    Characters.selectAll().map { rowToCharacter(it) }
}

private suspend fun handleGetCharacter(id: String?): Character? = dbQuery {
    if (id == null) return@dbQuery null
    Characters.selectAll().where { Characters.id eq id }.singleOrNull()?.let { rowToCharacter(it) }
}

private suspend fun handleSaveCharacter(char: Character?) = dbQuery {
    if (char == null) return@dbQuery
    Characters.upsert {
        it[id] = char.id
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
    updateLastModified()
}

private suspend fun handleDeleteCharacter(id: String?) = dbQuery {
    if (id == null) return@dbQuery
    Characters.deleteWhere { Characters.id eq id }
    updateLastModified()
}

private suspend fun handleGetLastModified(): String = dbQuery {
    Metadata.selectAll().where { Metadata.key eq "lastModified" }.singleOrNull()?.get(Metadata.value) ?: Instant.now().toString()
}

private suspend fun handleGetLocations(): List<Location> = dbQuery {
    Locations.selectAll().map {
        Location(it[Locations.id], it[Locations.name], it[Locations.description], it[Locations.imageUrl])
    }
}

private suspend fun handleSaveLocation(loc: Location?) = dbQuery {
    if (loc == null) return@dbQuery
    Locations.upsert {
        it[id] = loc.id
        it[name] = loc.name
        it[description] = loc.description
        it[imageUrl] = loc.imageUrl
    }
    updateLastModified()
}

private suspend fun handleDeleteLocation(id: String?) = dbQuery {
    if (id == null) return@dbQuery
    Locations.deleteWhere { Locations.id eq id }
    updateLastModified()
}

private suspend fun handleGetMonsters(): List<Monster> = dbQuery {
    Monsters.selectAll().map {
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

private suspend fun handleSaveMonster(m: Monster?) = dbQuery {
    if (m == null) return@dbQuery
    Monsters.upsert {
        it[id] = m.id
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
    updateLastModified()
}

private suspend fun handleDeleteMonster(id: String?) = dbQuery {
    if (id == null) return@dbQuery
    Monsters.deleteWhere { Monsters.id eq id }
    updateLastModified()
}

private suspend fun handleGetNpcs(): List<Npc> = dbQuery {
    Npcs.selectAll().map {
        Npc(it[Npcs.id], it[Npcs.name], it[Npcs.description], it[Npcs.imageUrl], it[Npcs.background])
    }
}

private suspend fun handleSaveNpc(n: Npc?) = dbQuery {
    if (n == null) return@dbQuery
    Npcs.upsert {
        it[id] = n.id
        it[name] = n.name
        it[description] = n.description
        it[imageUrl] = n.imageUrl
        it[background] = n.background
    }
    updateLastModified()
}

private suspend fun handleDeleteNpc(id: String?) = dbQuery {
    if (id == null) return@dbQuery
    Npcs.deleteWhere { Npcs.id eq id }
    updateLastModified()
}

private suspend fun handleGetMusic(): List<MusicTrack> = dbQuery {
    Music.selectAll().map { MusicTrack(it[Music.id], it[Music.name], it[Music.url]) }
}

private suspend fun handleSaveMusic(m: MusicTrack?) = dbQuery {
    if (m == null) return@dbQuery
    Music.upsert {
        it[id] = m.id
        it[name] = m.name
        it[url] = m.url
    }
    updateLastModified()
}

private suspend fun handleDeleteMusic(id: String?) = dbQuery {
    if (id == null) return@dbQuery
    Music.deleteWhere { Music.id eq id }
    updateLastModified()
}

private suspend fun handleGetLogs(): List<LogEntry> = dbQuery {
    Logs.selectAll().orderBy(Logs.timestamp, SortOrder.DESC).limit(100).map {
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

private suspend fun handleSaveLog(log: LogEntry?) = dbQuery {
    if (log == null) return@dbQuery
    Logs.insert {
        it[timestamp] = log.timestamp ?: Instant.now().toString()
        it[action] = log.action
        it[details] = log.details ?: ""
        it[initialState] = log.initialState?.toString() ?: ""
        it[endState] = log.endState?.toString() ?: ""
        it[success] = log.success
    }
}

private suspend fun handleGetEvents(): List<GameEvent> = dbQuery {
    Events.selectAll().map { GameEvent(it[Events.id], it[Events.name], it[Events.items]) }
}

private suspend fun handleSaveEvent(e: GameEvent?) = dbQuery {
    if (e == null) return@dbQuery
    Events.upsert {
        it[id] = e.id
        it[name] = e.name
        it[items] = e.items
    }
    updateLastModified()
}

private suspend fun handleDeleteEvent(id: String?) = dbQuery {
    if (id == null) return@dbQuery
    Events.deleteWhere { Events.id eq id }
    updateLastModified()
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
