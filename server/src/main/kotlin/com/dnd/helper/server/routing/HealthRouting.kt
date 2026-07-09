package com.dnd.helper.server.routing

import com.dnd.helper.server.database.DatabaseFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class HealthResponse(
    val status: String,
    val uptime: Long,
    val database: String? = null,
)

private val serverStartTime = System.currentTimeMillis()

fun Application.configureHealthRouting() {
    routing {
        route("/health") {
            /**
             * Liveness check — answers the question "is the process alive?".
             * Never touches the database. Load balancers / orchestrators use
             * this to decide whether to restart the container.
             */
            get {
                call.respond(
                    HttpStatusCode.OK,
                    HealthResponse(
                        status = "ok",
                        uptime = System.currentTimeMillis() - serverStartTime,
                    )
                )
            }

            /**
             * Readiness check — answers "can this instance serve traffic?".
             * Executes a lightweight DB ping. Returns 503 if the database is
             * unreachable so the load balancer stops routing traffic here.
             */
            get("/ready") {
                val dbStatus = try {
                    DatabaseFactory.dbQuery { transaction { exec("SELECT 1") } }
                    "ok"
                } catch (e: Exception) {
                    null
                }

                val httpStatus = if (dbStatus == "ok") HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
                call.respond(
                    httpStatus,
                    HealthResponse(
                        status = if (dbStatus == "ok") "ok" else "unavailable",
                        uptime = System.currentTimeMillis() - serverStartTime,
                        database = dbStatus ?: "unreachable",
                    )
                )
            }
        }
    }
}
