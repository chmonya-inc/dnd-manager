package com.dnd.helper.server.routing

import com.dnd.helper.data.remote.dto.auth.AssignCharacterRequest
import com.dnd.helper.data.remote.dto.auth.CampaignDto
import com.dnd.helper.data.remote.dto.auth.MyCharacterDto
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
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
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

                    call.respond(HttpStatusCode.OK, CampaignDto(campaignId, body.name, userId, body.sessionId))
                }

                // List all campaigns for the current master
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
                                it[Campaigns.id],
                                it[Campaigns.name],
                                it[Campaigns.ownerId],
                                it[Campaigns.sessionId]
                            )
                        }
                    }
                    call.respond(campaigns)
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

            // Get characters belonging to the current user (Player app)
            get("/api/my-characters") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val myChars = dbQuery {
                    Characters.selectAll().where { Characters.userId eq userId }.map { row ->
                        val sessionId = row[Characters.sessionId]
                        val campaignName = Campaigns.selectAll()
                            .where { Campaigns.sessionId eq sessionId }
                            .singleOrNull()?.get(Campaigns.name)
                        MyCharacterDto(
                            character = rowToCharacter(row),
                            sessionId = sessionId,
                            campaignName = campaignName
                        )
                    }
                }
                call.respond(myChars)
            }
        }
    }
}
