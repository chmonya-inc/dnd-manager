package com.dnd.helper.server.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {
    fun init() {
        val databasePath = "database.db"
        val dbFile = File(databasePath)
        
        Database.connect("jdbc:sqlite:$databasePath", "org.sqlite.JDBC")
        
        transaction {
            SchemaUtils.create(
                Characters,
                Locations,
                Monsters,
                Npcs,
                Music,
                Events,
                Logs,
                Metadata
            )
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
