package com.dnd.helper.server.routing

import com.dnd.helper.data.remote.dto.auth.AssignmentStatusDto
import com.dnd.helper.data.remote.dto.auth.CreateAssignmentRequest
import com.dnd.helper.data.remote.dto.auth.PendingAssignmentDto
import com.dnd.helper.data.remote.dto.auth.RespondAssignmentRequest
import com.dnd.helper.server.database.Campaigns
import com.dnd.helper.server.database.CharacterAssignments
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
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.upsert
import java.util.UUID

fun Application.configureAssignmentRouting() {
    routing {
        authenticate("auth-jwt") {
            // --- Master: Create assignment request (send to player) ---
            post("/api/assignments") {
                val principal = call.principal<JWTPrincipal>()
                val masterId = principal?.payload?.getClaim("userId")?.asString()
                if (masterId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val body = call.receive<CreateAssignmentRequest>()

                // Find player by username
                val player = dbQuery {
                    Users.selectAll().where { Users.username eq body.playerUsername }.singleOrNull()
                }
                if (player == null) {
                    call.respond(HttpStatusCode.NotFound, "Player '${body.playerUsername}' not found")
                    return@post
                }
                val playerId = player[Users.id]

                // Verify character exists in the session
                val charRow = dbQuery {
                    Characters.selectAll()
                        .where { (Characters.id eq body.characterId) and (Characters.sessionId eq body.sessionId) }
                        .singleOrNull()
                }
                if (charRow == null) {
                    call.respond(HttpStatusCode.NotFound, "Character not found in this session")
                    return@post
                }

                // Check for existing PENDING assignments for this character
                val existingPending = dbQuery {
                    CharacterAssignments.selectAll()
                        .where {
                            (CharacterAssignments.characterId eq body.characterId) and
                                (CharacterAssignments.status eq "PENDING")
                        }
                        .toList()
                }

                // Rule 1: Same player already has a pending assignment for this character → reject
                val samePlayerPending = existingPending.find { it[CharacterAssignments.playerId] == playerId }
                if (samePlayerPending != null) {
                    call.respond(
                        HttpStatusCode.Conflict,
                        "This character already has a pending assignment for player '${body.playerUsername}'"
                    )
                    return@post
                }

                // Rule 2: Different player has a pending assignment → auto-revoke it
                existingPending.forEach { pending ->
                    val oldAssignmentId = pending[CharacterAssignments.id]
                    dbQuery {
                        CharacterAssignments.update({ CharacterAssignments.id eq oldAssignmentId }) {
                            it[CharacterAssignments.status] = "REVOKED"
                            it[CharacterAssignments.respondedAt] = System.currentTimeMillis()
                        }
                    }
                    // Notify the old player that the assignment was revoked
                    SessionManager.notifyUpdate(body.sessionId, "assignment_revoked", body.characterId)
                }

                val assignmentId = UUID.randomUUID().toString()
                dbQuery {
                    CharacterAssignments.insert {
                        it[CharacterAssignments.id] = assignmentId
                        it[CharacterAssignments.characterId] = body.characterId
                        it[CharacterAssignments.sessionId] = body.sessionId
                        it[CharacterAssignments.masterId] = masterId
                        it[CharacterAssignments.playerId] = playerId
                        it[CharacterAssignments.status] = "PENDING"
                        it[CharacterAssignments.createdAt] = System.currentTimeMillis()
                        it[CharacterAssignments.respondedAt] = null
                    }
                }

                // Notify player in real-time via the session WebSocket
                SessionManager.notifyUpdate(body.sessionId, "assignment", assignmentId)

                call.respond(HttpStatusCode.OK, mapOf("assignmentId" to assignmentId))
            }

            // --- Player: Get my pending assignments ---
            get("/api/assignments/pending") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val assignments = dbQuery {
                    CharacterAssignments.innerJoin(Characters, { CharacterAssignments.characterId }, { Characters.id })
                        .selectAll()
                        .where { (CharacterAssignments.playerId eq userId) and (CharacterAssignments.status eq "PENDING") }
                        .map { row ->
                            val sessionId = row[CharacterAssignments.sessionId]
                            val campaignName = Campaigns.selectAll()
                                .where { Campaigns.sessionId eq sessionId }
                                .singleOrNull()?.get(Campaigns.name)
                            val masterUsername = Users.selectAll()
                                .where { Users.id eq row[CharacterAssignments.masterId] }
                                .singleOrNull()?.get(Users.username)

                            PendingAssignmentDto(
                                assignmentId = row[CharacterAssignments.id],
                                character = rowToCharacter(row),
                                sessionId = sessionId,
                                campaignName = campaignName,
                                status = row[CharacterAssignments.status],
                                masterUsername = masterUsername
                            )
                        }
                }
                call.respond(assignments)
            }

            // --- Player: Accept or Revoke an assignment ---
            post("/api/assignments/respond") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.getClaim("userId")?.asString()
                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }

                val body = call.receive<RespondAssignmentRequest>()

                val assignment = dbQuery {
                    CharacterAssignments.selectAll()
                        .where { CharacterAssignments.id eq body.assignmentId }
                        .singleOrNull()
                }
                if (assignment == null) {
                    call.respond(HttpStatusCode.NotFound, "Assignment not found")
                    return@post
                }

                // Only the intended player can respond
                if (assignment[CharacterAssignments.playerId] != userId) {
                    call.respond(HttpStatusCode.Forbidden, "You are not the intended player for this assignment")
                    return@post
                }

                // Can only respond to PENDING assignments
                if (assignment[CharacterAssignments.status] != "PENDING") {
                    call.respond(HttpStatusCode.Conflict, "Assignment already responded to")
                    return@post
                }

                val characterId = assignment[CharacterAssignments.characterId]
                val sessionId = assignment[CharacterAssignments.sessionId]

                if (body.accept) {
                    // ACCEPTED: Create a template copy for the player AND link the campaign instance
                    val personalSession = "user-$userId"

                    dbQuery {
                        // 1. Mark assignment as accepted
                        CharacterAssignments.update({ CharacterAssignments.id eq body.assignmentId }) {
                            it[CharacterAssignments.status] = "ACCEPTED"
                            it[CharacterAssignments.respondedAt] = System.currentTimeMillis()
                        }

                        // 2. Fetch the full character data from the campaign instance
                        val charRow = Characters.selectAll()
                            .where { (Characters.id eq characterId) and (Characters.sessionId eq sessionId) }
                            .singleOrNull()

                        if (charRow != null) {
                            // 3. Create a template copy in the player's personal session
                            val templateId = UUID.randomUUID().toString()
                            Characters.upsert {
                                it[Characters.id] = templateId
                                it[Characters.sessionId] = personalSession
                                it[Characters.userId] = userId
                                it[Characters.name] = charRow[Characters.name]
                                it[Characters.playerName] = "" // Template has no instance link
                                it[Characters.race] = charRow[Characters.race]
                                it[Characters.characterClass] = charRow[Characters.characterClass]
                                it[Characters.level] = charRow[Characters.level]
                                it[Characters.description] = charRow[Characters.description]
                                it[Characters.imageUrl] = charRow[Characters.imageUrl]
                                it[Characters.maxHp] = charRow[Characters.maxHp]
                                it[Characters.currentHp] = charRow[Characters.currentHp]
                                it[Characters.strength] = charRow[Characters.strength]
                                it[Characters.dexterity] = charRow[Characters.dexterity]
                                it[Characters.constitution] = charRow[Characters.constitution]
                                it[Characters.intelligence] = charRow[Characters.intelligence]
                                it[Characters.wisdom] = charRow[Characters.wisdom]
                                it[Characters.charisma] = charRow[Characters.charisma]
                                it[Characters.subclass] = charRow[Characters.subclass]
                                it[Characters.background] = charRow[Characters.background]
                                it[Characters.experiencePoints] = charRow[Characters.experiencePoints]
                                it[Characters.appearance] = charRow[Characters.appearance]
                                it[Characters.combat] = charRow[Characters.combat]
                                it[Characters.proficiencies] = charRow[Characters.proficiencies]
                                it[Characters.weapons] = charRow[Characters.weapons]
                                it[Characters.features] = charRow[Characters.features]
                                it[Characters.spells] = charRow[Characters.spells]
                                it[Characters.items] = charRow[Characters.items]
                                it[Characters.notes] = charRow[Characters.notes]
                            }

                            // 4. Link the campaign instance to the new template
                            Characters.update(
                                { (Characters.id eq characterId) and (Characters.sessionId eq sessionId) }
                            ) {
                                it[Characters.userId] = userId
                                it[Characters.playerName] = "instance:$templateId"
                            }
                        }
                    }
                    // Notify both the master's session and the player's personal session
                    SessionManager.notifyUpdate(sessionId, "assignment_accepted", characterId)
                    SessionManager.notifyUpdate(sessionId, "characters", characterId)
                    SessionManager.notifyUpdate(personalSession, "characters", characterId)
                    call.respond(HttpStatusCode.OK)
                } else {
                    // REVOKED: mark assignment as revoked and clean up player data
                    dbQuery {
                        CharacterAssignments.update({ CharacterAssignments.id eq body.assignmentId }) {
                            it[CharacterAssignments.status] = "REVOKED"
                            it[CharacterAssignments.respondedAt] = System.currentTimeMillis()
                        }
                        // Find the template ID linked to this instance (if accepted previously)
                        val instanceRow = Characters.selectAll()
                            .where { (Characters.id eq characterId) and (Characters.sessionId eq sessionId) }
                            .singleOrNull()
                        val playerNameVal = instanceRow?.get(Characters.playerName) ?: ""
                        if (playerNameVal.startsWith("instance:")) {
                            val templateId = playerNameVal.removePrefix("instance:")
                            // Delete the template from player's personal session
                            Characters.deleteWhere {
                                (Characters.id eq templateId) and (Characters.sessionId eq "user-$userId")
                            }
                        }
                        // Unassign the campaign instance
                        Characters.update({ (Characters.id eq characterId) and (Characters.sessionId eq sessionId) }) {
                            it[Characters.userId] = null
                            it[Characters.playerName] = ""
                        }
                    }
                    SessionManager.notifyUpdate(sessionId, "assignment_revoked", characterId)
                    SessionManager.notifyUpdate(sessionId, "characters", characterId)
                    SessionManager.notifyUpdate("user-$userId", "characters", characterId)
                    call.respond(HttpStatusCode.OK)
                }
            }

            // --- Master: Get assignment statuses for characters in a session ---
            get("/api/assignments/status/{sessionId}") {
                val principal = call.principal<JWTPrincipal>()
                val masterId = principal?.payload?.getClaim("userId")?.asString()
                if (masterId == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val sessionId = call.parameters["sessionId"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                val statuses = dbQuery {
                    CharacterAssignments.innerJoin(Characters, { CharacterAssignments.characterId }, { Characters.id })
                        .selectAll()
                        .where { (CharacterAssignments.sessionId eq sessionId) and (CharacterAssignments.masterId eq masterId) }
                        .orderBy(CharacterAssignments.createdAt, SortOrder.DESC)
                        .map { row ->
                            val playerUsername = row[Characters.userId]?.let { uid ->
                                Users.selectAll().where { Users.id eq uid }.singleOrNull()?.get(Users.username)
                            }
                            AssignmentStatusDto(
                                assignmentId = row[CharacterAssignments.id],
                                characterId = row[CharacterAssignments.characterId],
                                characterName = row[Characters.name],
                                sessionId = row[CharacterAssignments.sessionId],
                                status = row[CharacterAssignments.status],
                                playerUsername = playerUsername
                            )
                        }
                }
                call.respond(statuses)
            }
        }
    }
}
