package com.dnd.helper.server.routing

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

object SessionManager {
    private val sessions = ConcurrentHashMap<String, MutableSet<DefaultWebSocketServerSession>>()

    fun addClient(sessionId: String, session: DefaultWebSocketServerSession) {
        sessions.computeIfAbsent(sessionId) { mutableSetOf() }.add(session)
    }

    fun removeClient(sessionId: String, session: DefaultWebSocketServerSession) {
        sessions[sessionId]?.remove(session)
        if (sessions[sessionId]?.isEmpty() == true) {
            sessions.remove(sessionId)
        }
    }

    suspend fun notifyUpdate(sessionId: String, updateType: String) {
        val clients = sessions[sessionId] ?: return
        clients.forEach { client ->
            try {
                client.send(Frame.Text("update:$updateType"))
            } catch (e: Exception) {
                // Client might be disconnected
            }
        }
    }
}
