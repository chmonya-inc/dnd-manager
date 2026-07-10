package com.dnd.helper.server

import com.dnd.helper.server.database.DatabaseFactory
import com.dnd.helper.server.routing.configureApiRouting
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.dnd.helper.server.routing.configureAuthRouting
import com.dnd.helper.server.routing.configureAssignmentRouting
import com.dnd.helper.server.routing.configureCampaignRouting
import com.dnd.helper.server.routing.configureHealthRouting
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 9090, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(IgnoreTrailingSlash)

    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
            encodeDefaults = true
        })
    }

    install(WebSockets) {
        pingPeriod = 1.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(RateLimit) {
        global {
            rateLimiter(limit = 100, refillPeriod = 60.seconds)
        }
        register(RateLimitName("auth")) {
            rateLimiter(limit = 5, refillPeriod = 60.seconds)
        }
    }

    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("ngrok-skip-browser-warning")
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            println("[StatusPages] Unhandled exception: ${cause::class.simpleName}: ${cause.message}")
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, cause.message ?: "Internal server error")
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {
            authHeader { call ->
                val header = call.request.parseAuthorizationHeader()
                if (header != null) return@authHeader header
                val wsToken = call.request.headers["Sec-WebSocket-Protocol"]
                if (wsToken != null) return@authHeader io.ktor.http.auth.HttpAuthHeader.Single("Bearer", wsToken)
                null
            }
            verifier(
                com.auth0.jwt.JWT.require(com.auth0.jwt.algorithms.Algorithm.HMAC256(com.dnd.helper.server.routing.jwtSecret))
                    .withIssuer(com.dnd.helper.server.routing.JWT_ISSUER)
                    .build()
            )
            validate { credential ->
                val isAccessToken = credential.payload.getClaim("type").asString() == "access"
                val userId = credential.payload.getClaim("userId").asString()
                if (isAccessToken && userId.isNotEmpty()) {
                    io.ktor.server.auth.jwt.JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    configureHealthRouting()
    configureAuthRouting()
    configureCampaignRouting()
    configureAssignmentRouting()

    routing {
        configureApiRouting()
    }
}
