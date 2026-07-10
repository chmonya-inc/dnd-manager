package com.dnd.helper.server.routing

import io.ktor.server.plugins.ratelimit.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.dnd.helper.data.remote.dto.auth.AuthResponse
import com.dnd.helper.data.remote.dto.auth.LoginRequest
import com.dnd.helper.data.remote.dto.auth.RefreshRequest
import com.dnd.helper.data.remote.dto.auth.RegisterRequest
import com.dnd.helper.data.remote.dto.auth.UserDto
import com.dnd.helper.server.database.RefreshTokens
import com.dnd.helper.server.database.Users
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import java.security.MessageDigest
import java.util.*

const val JWT_ISSUER = "dnd-helper-server"
val jwtSecret: String = System.getenv("JWT_SECRET")
    ?: error("JWT_SECRET environment variable must be set")

private const val ACCESS_TOKEN_EXPIRY_MS = 900_000L      // 15 minutes
private const val REFRESH_TOKEN_EXPIRY_MS = 7 * 86_400_000L // 7 days

private val hmac = Algorithm.HMAC256(jwtSecret)

private fun hashPassword(password: String): String =
    BCrypt.hashpw(password, BCrypt.gensalt(10))

private fun verifyPassword(password: String, hash: String): Boolean =
    try {
        BCrypt.checkpw(password, hash)
    } catch (_: Exception) {
        false
    }

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

/** SHA-256 hex of the raw JWT string — stored in DB instead of the raw token. */
private fun sha256Hex(token: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(token.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
}

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

/**
 * Store a hashed refresh token in the DB.
 * Any expired rows for this user are pruned at the same time to keep the table lean.
 */
private fun storeRefreshToken(token: String, userId: String) {
    val hash = sha256Hex(token)
    val expiresAt = System.currentTimeMillis() + REFRESH_TOKEN_EXPIRY_MS
    val now = System.currentTimeMillis()
    transaction {
        // Prune expired tokens for this user
        RefreshTokens.deleteWhere {
            (RefreshTokens.userId eq userId) and (RefreshTokens.expiresAt less now)
        }
        RefreshTokens.insert {
            it[tokenHash] = hash
            it[RefreshTokens.userId] = userId
            it[RefreshTokens.expiresAt] = expiresAt
        }
    }
}

/**
 * Revoke a single refresh token (called on logout or rotation).
 * Returns true if a row was actually deleted.
 */
private fun revokeRefreshToken(token: String): Boolean {
    val hash = sha256Hex(token)
    val deleted = transaction {
        RefreshTokens.deleteWhere { tokenHash eq hash }
    }
    return deleted > 0
}

/**
 * Check that [token] exists in the DB and has not expired.
 * Returns the stored userId on success, null otherwise.
 */
private fun validateStoredRefreshToken(token: String): String? {
    val hash = sha256Hex(token)
    val now = System.currentTimeMillis()
    val row = transaction {
        RefreshTokens
            .selectAll()
            .where { (RefreshTokens.tokenHash eq hash) and (RefreshTokens.expiresAt greaterEq now) }
            .singleOrNull()
    }
    return row?.get(RefreshTokens.userId)
}


fun Application.configureAuthRouting() {
    routing {
        rateLimit(RateLimitName("auth")) {
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
                    val hash = hashPassword(request.password)

                    val role = if (request.role == "MASTER") "MASTER" else "PLAYER"

                    transaction {
                        Users.insert {
                            it[id] = userId
                            it[username] = request.username
                            it[passwordHash] = hash
                            it[Users.role] = role
                        }
                    }

                    val accessToken = generateAccessToken(userId)
                    val refreshToken = generateRefreshToken(userId)
                    storeRefreshToken(refreshToken, userId)

                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(accessToken, refreshToken, UserDto(userId, request.username, role))
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
                    if (!verifyPassword(request.password, hash)) {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                        return@post
                    }

                    val userId = userRow[Users.id]
                    val userRole = userRow[Users.role]

                    val accessToken = generateAccessToken(userId)
                    val refreshToken = generateRefreshToken(userId)
                    storeRefreshToken(refreshToken, userId)

                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(accessToken, refreshToken, UserDto(userId, request.username, userRole))
                    )
                }

                post("/refresh") {
                    val request = call.receive<RefreshRequest>()

                    // 1. Verify JWT signature & expiry
                    val userIdFromJwt = verifyRefreshToken(request.refreshToken)
                    if (userIdFromJwt == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid or expired refresh token")
                        return@post
                    }

                    // 2. Verify the token exists in the DB (not revoked)
                    val userIdFromDb = validateStoredRefreshToken(request.refreshToken)
                    if (userIdFromDb == null) {
                        call.respond(HttpStatusCode.Unauthorized, "Refresh token has been revoked")
                        return@post
                    }

                    val userRow = transaction {
                        Users.selectAll().where { Users.id eq userIdFromDb }.singleOrNull()
                    }
                    if (userRow == null) {
                        call.respond(HttpStatusCode.Unauthorized, "User not found")
                        return@post
                    }

                    // 3. Rotate: revoke old token, issue new pair
                    revokeRefreshToken(request.refreshToken)

                    val username = userRow[Users.username]
                    val userRole = userRow[Users.role]
                    val newAccessToken = generateAccessToken(userIdFromDb)
                    val newRefreshToken = generateRefreshToken(userIdFromDb)
                    storeRefreshToken(newRefreshToken, userIdFromDb)

                    call.respond(
                        HttpStatusCode.OK,
                        AuthResponse(newAccessToken, newRefreshToken, UserDto(userIdFromDb, username, userRole))
                    )
                }

                /** Revoke the supplied refresh token so it can never be used again. */
                post("/logout") {
                    val request = runCatching { call.receive<RefreshRequest>() }.getOrNull()
                    if (request != null) {
                        revokeRefreshToken(request.refreshToken)
                    }
                    call.respond(HttpStatusCode.OK, "Logged out")
                }
            }
        }
    }
}
