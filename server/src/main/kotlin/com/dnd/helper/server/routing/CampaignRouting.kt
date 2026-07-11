package com.dnd.helper.server.routing

import com.dnd.helper.data.remote.dto.auth.AssignCharacterRequest
import com.dnd.helper.data.remote.dto.auth.CampaignDto
import com.dnd.helper.data.remote.dto.auth.CharacterTemplateDto
import com.dnd.helper.data.remote.dto.auth.JoinCampaignRequest
import com.dnd.helper.data.remote.dto.auth.MyCharacterDto
import com.dnd.helper.data.remote.dto.auth.MyCharactersResponse
import com.dnd.helper.domain.model.Character
import com.dnd.helper.server.database.Campaigns
import com.dnd.helper.server.database.Characters
import com.dnd.helper.server.database.DatabaseFactory.dbQuery
import com.dnd.helper.server.database.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.not
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import java.util.UUID

fun Application.configureCampaignRouting() {
    routing {
        authenticate("auth-jwt") {
            route("/api/campaigns") {
                // Create a campaign (Master only)
                post {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@post
                    }

                    val userRole = transaction {
                        Users.selectAll().where { Users.id eq userId }.singleOrNull()?.get(Users.role)
                    }
                    if (userRole != "MASTER") {
                        call.respond(HttpStatusCode.Forbidden, "Only masters can create campaigns")
                        return@post
                    }

                    val body = call.receive<CampaignDto>()
                    val campaignId = UUID.randomUUID().toString()

                    transaction {
                        Campaigns.insert {
                            it[Campaigns.id] = campaignId
                            it[Campaigns.name] = body.name
                            it[Campaigns.ownerId] = userId
                            it[Campaigns.sessionId] = body.sessionId
                        }
                    }

                    call.respond(HttpStatusCode.OK, CampaignDto(campaignId, body.name, userId, body.sessionId, false))
                }

                // Get campaigns for Master
                get {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@get
                    }

                    val campaigns = transaction {
                        Campaigns.selectAll().where { Campaigns.ownerId eq userId }.map {
                            CampaignDto(
                                id = it[Campaigns.id],
                                name = it[Campaigns.name],
                                ownerId = it[Campaigns.ownerId],
                                sessionId = it[Campaigns.sessionId],
                                isStarted = it[Campaigns.isStarted]
                            )
                        }
                    }
                    call.respond(campaigns)
                }

                // Start/Stop a campaign
                post("/{id}/toggle-start") {
                    val principal = call.principal<JWTPrincipal>()
                    val userId = principal?.payload?.getClaim("userId")?.asString()
                    if (userId == null) {
                        call.respond(HttpStatusCode.Unauthorized)
                        return@post
                    }

                    val idParam = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val body = call.receive<Map<String, Boolean>>()
                    val isStarted = body["isStarted"] ?: false

                    val result = transaction {
                        // Match by either campaign UUID or sessionId (client uses sessionId)
                        val campaign = Campaigns.selectAll()
                            .where { (Campaigns.id eq idParam) or (Campaigns.sessionId eq idParam) }
                            .singleOrNull()
                        if (campaign == null || campaign[Campaigns.ownerId] != userId) {
                            false
                        } else {
                            Campaigns.update({ Campaigns.id eq campaign[Campaigns.id] }) {
                                it[Campaigns.isStarted] = isStarted
                            }
                            true
                        }
                    }

                    if (result) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

            // Assign a character to a user (Master only)
            post("/api/characters/assign") {
                val principal = call.principal<JWTPrincipal>()
                val masterId = principal?.payload?.getClaim("userId")?.asString()
                println("[CampaignRouting] Assignment request from userId: $masterId")
                if (masterId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val userRole = transaction {
                    Users.selectAll().where { Users.id eq masterId }.singleOrNull()?.get(Users.role)
                }
                println("[CampaignRouting] User role: $userRole")
                if (userRole != "MASTER") {
                    call.respond(HttpStatusCode.Forbidden, "Only masters can assign characters")
                    return@post
                }

                val body = call.receive<AssignCharacterRequest>()
                println(
                    "[CampaignRouting] Assigning character ${body.characterId} in session ${body.sessionId} to user ${body.ownerUserId}"
                )

                val result = transaction {
                    val existing = Characters.selectAll()
                        .where { Characters.id eq body.characterId }
                        .singleOrNull()

                    if (existing == null) {
                        println("[CampaignRouting] Character not found for assignment: ${body.characterId}")
                        null
                    } else {
                        val charSessionId = existing[Characters.sessionId]
                        Characters.update({ Characters.id eq body.characterId }) {
                            it[Characters.userId] = body.ownerUserId
                        }
                        println(
                            "[CampaignRouting] Assigned character to user ${body.ownerUserId} in session $charSessionId"
                        )
                        charSessionId
                    }
                }

                if (result == null) {
                    call.respond(HttpStatusCode.NotFound, "Character not found")
                    return@post
                }

                SessionManager.notifyUpdate(result, "characters", body.characterId)
                call.respond(HttpStatusCode.OK)
            }

            // Assign a character by username (Master only)
            post("/api/characters/assign-by-username") {
                val principal = call.principal<JWTPrincipal>()
                val masterId = principal?.payload?.getClaim("userId")?.asString()
                if (masterId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val userRole = transaction {
                    Users.selectAll().where { Users.id eq masterId }.singleOrNull()?.get(Users.role)
                }
                if (userRole != "MASTER") {
                    call.respond(HttpStatusCode.Forbidden, "Only masters can assign characters")
                    return@post
                }

                val body = call.receive<com.dnd.helper.data.remote.dto.auth.AssignByUsernameRequest>()

                val result = transaction {
                    // Find user by username
                    val username = body.username
                    val targetUserId = if (username != null) {
                        Users.selectAll().where { Users.username eq username }.singleOrNull()?.get(Users.id)
                            ?: return@transaction "USER_NOT_FOUND"
                    } else {
                        null
                    }

                    val existing = Characters.selectAll()
                        .where { Characters.id eq body.characterId }
                        .singleOrNull()

                    if (existing == null) {
                        "CHARACTER_NOT_FOUND"
                    } else {
                        val charSessionId = existing[Characters.sessionId]
                        Characters.update({ Characters.id eq body.characterId }) {
                            it[Characters.userId] = targetUserId
                        }
                        charSessionId
                    }
                }

                when (result) {
                    "USER_NOT_FOUND" -> call.respond(
                        HttpStatusCode.NotFound,
                        "User with username '${body.username}' not found"
                    )
                    "CHARACTER_NOT_FOUND" -> call.respond(HttpStatusCode.NotFound, "Character not found")
                    else -> {
                        SessionManager.notifyUpdate(result, "characters", body.characterId)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }

            // Get character templates + campaign instances for the current user (Player app)
            get("/api/my-characters") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val personalSession = "user-$userId"

                val templates = dbQuery {
                    Characters.selectAll()
                        .where { (Characters.userId eq userId) and (Characters.sessionId eq personalSession) }
                        .map { rowToCharacter(it) }
                }

                // All non-template rows owned by this user (campaign instances)
                val instances = dbQuery {
                    Characters.selectAll()
                        .where { (Characters.userId eq userId) and not(Characters.sessionId eq personalSession) }
                        .map { row ->
                            val sessionId = row[Characters.sessionId]
                            val campaign = Campaigns.selectAll()
                                .where { Campaigns.sessionId eq sessionId }
                                .singleOrNull()
                            val campaignName = campaign?.get(Campaigns.name)
                            val isGameStarted = campaign?.get(Campaigns.isStarted) ?: false

                            MyCharacterDto(
                                character = rowToCharacter(row),
                                sessionId = sessionId,
                                campaignName = campaignName,
                                isGameStarted = isGameStarted
                            )
                        }
                }

                val templateIds = templates.map { it.id }.toSet()

                // Group instances under their surviving template
                val grouped = templates.map { template ->
                    CharacterTemplateDto(
                        template = template,
                        instances = instances.filter {
                            it.character.playerName == "instance:${template.id}"
                        }
                    )
                }

                // Instances whose template was deleted (orphaned) — shown as standalone cards
                val standaloneInstances = instances.filter {
                    it.character.playerName.startsWith("instance:") &&
                        it.character.playerName.removePrefix("instance:") !in templateIds
                }

                call.respond(MyCharactersResponse(templates = grouped, standaloneInstances = standaloneInstances))
            }

            // Get a single character template from the player's personal session
            get("/api/my-characters/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val charId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                val personalSession = "user-$userId"

                val template = dbQuery {
                    Characters.selectAll()
                        .where {
                            (Characters.id eq charId) and
                                (Characters.sessionId eq personalSession) and
                                (Characters.userId eq userId)
                        }
                        .singleOrNull()?.let { rowToCharacter(it) }
                }

                if (template == null) call.respond(HttpStatusCode.NotFound) else call.respond(template)
            }

            // Player creates their own character (stored in personal session user-{userId})
            post("/api/my-characters") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val char = call.receive<Character>()
                val charId = if (char.id.isBlank()) UUID.randomUUID().toString() else char.id
                // ponytail: personal session = user-{userId}; ceiling is session isolation (can't websocket-watch own chars)
                val personalSession = "user-$userId"

                transaction {
                    Characters.upsert {
                        it[id] = charId
                        it[sessionId] = personalSession
                        it[Characters.userId] = userId
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
                call.respond(HttpStatusCode.OK, mapOf("id" to charId))
            }

            // Player joins a campaign: links their character into the game session
            post("/api/my-characters/{id}/join") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val charId = call.parameters["id"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                val body = call.receive<JoinCampaignRequest>()
                val gameId = body.gameId.trim()

                val result = transaction {
                    // Verify player owns this character
                    val existing = Characters.selectAll()
                        .where { (Characters.id eq charId) and (Characters.userId eq userId) }
                        .singleOrNull() ?: return@transaction "NOT_FOUND"

                    val currentSession = existing[Characters.sessionId]
                    if (currentSession == gameId) return@transaction "ALREADY_JOINED"

                    // Upsert into the game session (creates a copy if it doesn't exist, or updates if it does)
                    Characters.upsert {
                        it[id] = charId
                        it[sessionId] = gameId
                        it[Characters.userId] = userId
                        it[name] = existing[Characters.name]
                        it[playerName] = "instance:${existing[Characters.id]}" // Link to template ID
                        it[race] = existing[Characters.race]
                        it[characterClass] = existing[Characters.characterClass]
                        it[level] = existing[Characters.level]
                        it[description] = existing[Characters.description]
                        it[imageUrl] = existing[Characters.imageUrl]
                        it[maxHp] = existing[Characters.maxHp]
                        it[currentHp] = existing[Characters.currentHp]
                        it[strength] = existing[Characters.strength]
                        it[dexterity] = existing[Characters.dexterity]
                        it[constitution] = existing[Characters.constitution]
                        it[intelligence] = existing[Characters.intelligence]
                        it[wisdom] = existing[Characters.wisdom]
                        it[charisma] = existing[Characters.charisma]
                        it[subclass] = existing[Characters.subclass]
                        it[background] = existing[Characters.background]
                        it[experiencePoints] = existing[Characters.experiencePoints]
                        it[appearance] = existing[Characters.appearance]
                        it[combat] = existing[Characters.combat]
                        it[proficiencies] = existing[Characters.proficiencies]
                        it[weapons] = existing[Characters.weapons]
                        it[features] = existing[Characters.features]
                        it[spells] = existing[Characters.spells]
                        it[items] = existing[Characters.items]
                        it[notes] = existing[Characters.notes]
                    }

                    // We NO LONGER delete from old personal session.
                    // The character in user-{userId} remains as a template.

                    gameId
                }

                when (result) {
                    "NOT_FOUND" -> call.respond(HttpStatusCode.NotFound, "Character not found or not owned by you")
                    "ALREADY_JOINED" -> call.respond(HttpStatusCode.OK)
                    else -> {
                        SessionManager.notifyUpdate(result as String, "characters", charId)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }

            // Player deletes a character template. Campaign copies are intentionally kept.
            delete("/api/my-characters/{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@delete
                }

                val charId = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
                val personalSession = "user-$userId"

                transaction {
                    Characters.deleteWhere {
                        (Characters.id eq charId) and
                            (Characters.sessionId eq personalSession) and
                            (Characters.userId eq userId)
                    }
                }

                SessionManager.notifyUpdate(personalSession, "characters", charId)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
