package com.dnd.helper.server.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(isTest: Boolean = System.getProperty("isTest") == "true") {
        val host = System.getenv("DB_HOST") ?: "localhost"
        val port = System.getenv("DB_PORT") ?: "5432"
        val dbName = System.getenv("DB_NAME") ?: "dndhelper"
        val user = System.getenv("DB_USER") ?: "postgres"
        val password = System.getenv("DB_PASSWORD") ?: "postgres"

        val url = if (isTest) {
            System.getProperty("DB_URL") ?: "jdbc:postgresql://$host:$port/$dbName"
        } else {
            "jdbc:postgresql://$host:$port/$dbName"
        }

        while (true) {
            try {
                println("[DatabaseFactory] Connecting to PostgreSQL at $url...")

                Database.connect(
                    url = url,
                    driver = "org.postgresql.Driver",
                    user = user,
                    password = password
                )

                transaction {
                    // Manual migration: skills -> spells
                    // Must run before createMissingTablesAndColumns if we want to RENAME instead of DROP/ADD
                    try {
                        exec(
                            """
                            DO ${'$'}${'$'}
                            BEGIN
                              -- If skills exists but spells does not, rename it
                              IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='characters' AND column_name='skills') AND 
                                 NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='characters' AND column_name='spells') THEN
                                ALTER TABLE characters RENAME COLUMN skills TO spells;
                              END IF;
                              
                              -- If both exist, migrate data and drop skills
                              IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='characters' AND column_name='skills') AND 
                                 EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='characters' AND column_name='spells') THEN
                                UPDATE characters SET spells = skills WHERE (spells IS NULL OR spells::text = '[]') AND skills IS NOT NULL;
                                ALTER TABLE characters DROP COLUMN skills;
                              END IF;
                              
                              -- Ensure spells has no nulls if it exists
                              IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='characters' AND column_name='spells') THEN
                                UPDATE characters SET spells = '[]' WHERE spells IS NULL;
                              END IF;
                            END ${'$'}${'$'};
                            """.trimIndent()
                        )
                        println("[DatabaseFactory] Checked and applied 'skills' to 'spells' migration if needed")
                    } catch (e: Exception) {
                        println("[DatabaseFactory] Migration logic skipped or failed: ${e.message}")
                    }

                    SchemaUtils.createMissingTablesAndColumns(
                        RefreshTokens,
                        Users,
                        Campaigns,
                        Characters,
                        Locations,
                        Battlefields,
                        Monsters,
                        Npcs,
                        Music,
                        Events,
                        Logs,
                        CharacterAssignments
                    )
                }
                println("[DatabaseFactory] Successfully connected to PostgreSQL")
                break // Exit the retry loop on success
            } catch (e: Exception) {
                println("[DatabaseFactory] Failed to connect or initialize database: ${e.message}")
                if (isTest) {
                    throw e // Don't retry in tests
                }
                println("[DatabaseFactory] Retrying in 5 seconds...")
                Thread.sleep(5000)
            }
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
