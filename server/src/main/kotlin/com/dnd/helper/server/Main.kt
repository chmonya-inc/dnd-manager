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
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import com.dnd.helper.server.routing.configureAuthRouting
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = 9090, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    DatabaseFactory.init()

    install(ContentNegotiation) {
        json()
    }

    install(WebSockets) {
        pingPeriod = 1.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
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

    install(Authentication) {
        jwt("auth-jwt") {
            authHeader { call ->
                val header = call.request.parseAuthorizationHeader()
                val token = call.request.queryParameters["token"]
                if (header != null) return@authHeader header
                if (token != null) return@authHeader io.ktor.http.auth.HttpAuthHeader.Single("Bearer", token)
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

    routing {
        get("/") {
            call.respondText("D&D Helper Server is running!")
        }
        configureAuthRouting()
        configureApiRouting()
    }
}
