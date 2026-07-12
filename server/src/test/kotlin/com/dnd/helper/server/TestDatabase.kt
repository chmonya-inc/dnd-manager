package com.dnd.helper.server

import com.dnd.helper.server.database.DatabaseFactory
import org.testcontainers.containers.PostgreSQLContainer

object TestDatabase {
    private val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("testdb")
        withUsername("testuser")
        withPassword("testpass")
    }

    fun start() {
        if (!postgres.isRunning) {
            postgres.start()
            System.setProperty("isTest", "true")
            System.setProperty("DB_URL", postgres.jdbcUrl)
            System.setProperty("DB_USER", postgres.username)
            System.setProperty("DB_PASSWORD", postgres.password)
            
            // Initialize the database once
            DatabaseFactory.init()
        }
    }

    fun stop() {
        postgres.stop()
    }
}
