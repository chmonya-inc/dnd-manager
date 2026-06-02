package com.dnd.helper.server.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {
    fun init() {
        val host = System.getenv("DB_HOST") ?: "localhost"
        val port = System.getenv("DB_PORT") ?: "5432"
        val dbName = System.getenv("DB_NAME") ?: "dndhelper"
        val user = System.getenv("DB_USER") ?: "postgres"
        val password = System.getenv("DB_PASSWORD") ?: "postgres"

        val url = "jdbc:postgresql://$host:$port/$dbName"
        
        println("[DatabaseFactory] Connecting to PostgreSQL at $url...")
        
        Database.connect(
            url = url,
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )
        
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Characters,
                Locations,
                Battlefields,
                Monsters,
                Npcs,
                Music,
                Events,
                Logs
            )
        }
        println("[DatabaseFactory] Successfully connected to PostgreSQL")
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
