package com.dnd.helper.server.routing

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.dnd.helper.data.remote.dto.auth.AuthResponse
import com.dnd.helper.data.remote.dto.auth.LoginRequest
import com.dnd.helper.data.remote.dto.auth.RefreshRequest
import com.dnd.helper.data.remote.dto.auth.RegisterRequest
import com.dnd.helper.data.remote.dto.auth.UserDto
import com.dnd.helper.server.database.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.mindrot.jbcrypt.BCrypt
import java.util.*

const val JWT_ISSUER = "dnd-helper-server"
val jwtSecret: String = System.getenv("JWT_SECRET")
    ?: error("JWT_SECRET environment variable must be set")

private const val ACCESS_TOKEN_EXPIRY_MS = 900_000L      // 15 minutes
private const val REFRESH_TOKEN_EXPIRY_MS = 7 * 86_400_000L // 7 days

private val hmac = Algorithm.HMAC256(jwtSecret)

private fun generateAccessToken(userId: String): String =
    JWT.create()
        .withIssuer(JWT_ISSUER)
        .withClaim("userId", userId)
        .withClaim("type", "access")
        .withExpiresAt(Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS))
        .sign(hmac)

private fun generateRefreshToken(userId: String): String =
    JWT.create()
        .withIssuer(JWT_ISSUER)
        .withClaim("userId", userId)
        .withClaim("type", "refresh")
        .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY_MS))
        .sign(hmac)

private fun verifyRefreshToken(token: String): String? =
    try {
        val decoded = JWT.require(hmac)
            .withIssuer(JWT_ISSUER)
            .build()
            .verify(token)
        if (decoded.getClaim("type").asString() == "refresh") {
            decoded.getClaim("userId").asString()
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }

fun Application.configureAuthRouting() {
    routing {
        route("/auth") {
            post("/register") {
                val request = call.receive<RegisterRequest>()

                if (request.username.isBlank() || request.password.length < 6) {
                    call.respond(HttpStatusCode.BadRequest, "Username cannot be empty and password must be at least 6 characters")
                    return@post
                }

                val existingUser = transaction {
                    Users.selectAll().where { Users.username eq request.username }.singleOrNull()
                }
                if (existingUser != null) {
                    call.respond(HttpStatusCode.Conflict, "Username already exists")
                    return@post
                }

                val userId = UUID.randomUUID().toString()
                val hash = BCrypt.hashpw(request.password, BCrypt.gensalt())

                val role = if (request.role == "MASTER") "MASTER" else "PLAYER"

                transaction {
                    Users.insert {
                        it[id] = userId
                        it[username] = request.username
                        it[passwordHash] = hash
                        it[Users.role] = role
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    AuthResponse(generateAccessToken(userId), generateRefreshToken(userId), UserDto(userId, request.username, role))
                )
            }

            post("/login") {
                val request = call.receive<LoginRequest>()

                val userRow = transaction {
                    Users.selectAll().where { Users.username eq request.username }.singleOrNull()
                }

                if (userRow == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                    return@post
                }

                val hash = userRow[Users.passwordHash]
                if (!BCrypt.checkpw(request.password, hash)) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                    return@post
                }

                val userId = userRow[Users.id]
                val userRole = userRow[Users.role]
                call.respond(
                    HttpStatusCode.OK,
                    AuthResponse(generateAccessToken(userId), generateRefreshToken(userId), UserDto(userId, request.username, userRole))
                )
            }

            post("/refresh") {
                val request = call.receive<RefreshRequest>()
                val userId = verifyRefreshToken(request.refreshToken)

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid or expired refresh token")
                    return@post
                }

                val userRow = transaction {
                    Users.selectAll().where { Users.id eq userId }.singleOrNull()
                }
                if (userRow == null) {
                    call.respond(HttpStatusCode.Unauthorized, "User not found")
                    return@post
                }

                val username = userRow[Users.username]
                val userRole = userRow[Users.role]
                call.respond(
                    HttpStatusCode.OK,
                    AuthResponse(generateAccessToken(userId), generateRefreshToken(userId), UserDto(userId, username, userRole))
                )
            }
        }
    }
}
