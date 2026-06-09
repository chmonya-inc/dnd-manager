package com.dnd.helper.di

import com.dnd.helper.domain.music.AudioPlayer
import com.dnd.helper.domain.storage.CharacterStorage
import org.koin.dsl.module
import java.util.prefs.Preferences
import kotlinx.serialization.json.*
import kotlinx.serialization.*

class DesktopAudioPlayer : AudioPlayer {
    private var player: javazoom.jl.player.Player? = null
    private var currentUrl: String? = null
    private var isPaused = false
    private var lastPositionMs: Long = 0
    private var startTimeMs: Long = 0
    private var estimatedDurationMs: Long = 0
    private var bitrate: Int = 0

    override fun play(url: String) {
        stop()
        currentUrl = url
        // Estimate duration in background
        kotlin.concurrent.thread {
            estimateDuration(url)
        }
        startThread(0)
    }

    private fun estimateDuration(urlStr: String) {
        try {
            val url = java.net.URI(urlStr).toURL()
            val connection = url.openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            
            val contentLength = connection.contentLengthLong
            if (contentLength <= 0) return

            val stream = java.io.BufferedInputStream(connection.getInputStream())
            val bitstream = javazoom.jl.decoder.Bitstream(stream)
            val header = bitstream.readFrame()
            bitrate = header.bitrate()
            bitstream.close()

            if (bitrate > 0) {
                estimatedDurationMs = (contentLength * 8 * 1000) / bitrate
            } else {
                bitrate = 128000
                estimatedDurationMs = (contentLength / 16000) * 1000 // Fallback 128kbps
            }
        } catch (e: Exception) {
            bitrate = 128000
            estimatedDurationMs = 0
        }
    }

    private fun startThread(skipMs: Long) {
        kotlin.concurrent.thread {
            try {
                val url = java.net.URI(currentUrl!!).toURL()
                val connection = url.openConnection()
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                
                val inputStream = java.io.BufferedInputStream(connection.getInputStream())
                if (skipMs > 0 && bitrate > 0) {
                    // Skip precise bytes based on detected bitrate
                    // bytes = (ms * bitrate_bps) / (8 bits * 1000 ms)
                    val bytesToSkip = (skipMs * bitrate) / 8000
                    inputStream.skip(bytesToSkip)
                }
                
                val p = javazoom.jl.player.Player(inputStream)
                player = p
                startTimeMs = System.currentTimeMillis() - skipMs
                isPaused = false
                p.play()
                
                // When play() returns, the song is finished naturally
                if (player == p) {
                    lastPositionMs = if (estimatedDurationMs > 0) estimatedDurationMs else 0
                    player = null
                    isPaused = false
                }
            } catch (e: Exception) {
                player = null
            }
        }
    }

    override fun pause() {
        if (player != null && !isPaused) {
            lastPositionMs = getCurrentPosition()
            player?.close()
            isPaused = true
        }
    }

    override fun resume() {
        if (isPaused && currentUrl != null) {
            startThread(lastPositionMs)
        }
    }

    override fun stop() {
        player?.close()
        player = null
        isPaused = false
        lastPositionMs = 0
        // Don't wipe estimatedDurationMs here, wipe it in play() for new track
    }

    override fun setVolume(volume: Float) {}

    override fun isPlaying(): Boolean = player != null && !isPaused

    override fun seekTo(position: Long) {
        if (currentUrl != null) {
            val wasPlaying = isPlaying()
            // Surgical stop: close player but don't reset lastPositionMs yet
            player?.close()
            player = null
            
            lastPositionMs = position
            if (wasPlaying || isPaused) {
                startThread(position)
                if (isPaused) pause() // stay paused if it was paused
            }
        }
    }

    override fun getCurrentPosition(): Long {
        val pos = if (isPlaying()) {
            System.currentTimeMillis() - startTimeMs
        } else {
            lastPositionMs
        }
        return if (estimatedDurationMs > 0) pos.coerceAtMost(estimatedDurationMs) else pos
    }

    override fun getDuration(): Long = estimatedDurationMs
}

class DesktopCharacterStorage : CharacterStorage {
    private val prefs = Preferences.userRoot().node("com.dnd.helper")
    
    override fun saveCharacterId(id: String) {
        prefs.put("last_character_id", id)
    }

    override fun getCharacterId(): String? {
        return prefs.get("last_character_id", null)
    }

    override fun saveTableId(id: String) {
        prefs.put("last_table_id", id)
    }

    override fun getTableId(): String? {
        return prefs.get("last_table_id", null)
    }

    override fun saveSessions(sessionsJson: String) {
        prefs.put("saved_sessions", sessionsJson)
    }

    override fun getSessions(): String? {
        return prefs.get("saved_sessions", null)
    }

    override fun saveTheme(themeName: String) {
        prefs.put("app_theme", themeName)
    }

    override fun getTheme(): String? {
        return prefs.get("app_theme", null)
    }

    override fun saveComfyUiAddress(address: String) {
        prefs.put("comfy_ui_address", address)
    }

    override fun getComfyUiAddress(): String? {
        return prefs.get("comfy_ui_address", null)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun saveComfyUi(workflow: JsonObject) {
        try {
            val dir = java.io.File(System.getProperty("user.home"), ".dndhelper")
            if (!dir.exists()) dir.mkdirs()
            val file = java.io.File(dir, "workflow.json")
            file.outputStream().use { stream ->
                Json.encodeToStream(workflow, stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun getComfyUiWorkflow(): JsonObject? {
        return try {
            val file = java.io.File(System.getProperty("user.home"), ".dndhelper/workflow.json")
            if (file.exists()) {
                file.inputStream().use { stream ->
                    Json.decodeFromStream<JsonObject>(stream)
                }
            } else null
        } catch (e: Exception) {
            null
        }
    }

    override fun saveGenerationSteps(steps: Int) {
        prefs.putInt("gen_steps", steps)
    }

    override fun getGenerationSteps(): Int {
        return prefs.getInt("gen_steps", 20)
    }

    override fun saveApiCache(key: String, json: String) {
        try {
            val dir = java.io.File(System.getProperty("user.home"), ".dndhelper/cache")
            if (!dir.exists()) dir.mkdirs()
            // Clean the key to be a safe filename
            val safeKey = key.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            java.io.File(dir, safeKey).writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getApiCache(key: String): String? {
        return try {
            val safeKey = key.replace(Regex("[^a-zA-Z0-9.-]"), "_")
            val file = java.io.File(System.getProperty("user.home"), ".dndhelper/cache/$safeKey")
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            null
        }
    }

    override fun clearApiCache() {
        try {
            val dir = java.io.File(System.getProperty("user.home"), ".dndhelper/cache")
            if (dir.exists()) {
                dir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

actual val platformModule = module {
    single<CharacterStorage> { DesktopCharacterStorage() }
    single<AudioPlayer> { DesktopAudioPlayer() }
}

actual val isDesktop: Boolean = true
actual val isWeb: Boolean = false

actual fun openUrl(url: String) {
    try {
        val desktop = if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop() else null
        if (desktop != null && desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            desktop.browse(java.net.URI(url))
        } else {
            // Fallback for some Linux environments
            Runtime.getRuntime().exec("xdg-open $url")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

actual fun pickFile(title: String, allowedExtensions: List<String>): String? {
    val activeWindow = java.awt.Window.getWindows()
        .filterIsInstance<java.awt.Frame>()
        .find { it.isVisible && it.title == "D&D Helper" }
        ?: java.awt.Window.getWindows().find { it.isVisible && it is java.awt.Frame } as? java.awt.Frame

    val dialog = java.awt.FileDialog(activeWindow, title, java.awt.FileDialog.LOAD)
    if (allowedExtensions.isNotEmpty()) {
        dialog.setFilenameFilter { _, name -> 
            allowedExtensions.any { name.lowercase().endsWith(it.lowercase()) } 
        }
    }
    
    dialog.isAlwaysOnTop = true
    dialog.isVisible = true
    return if (dialog.file != null) dialog.directory + dialog.file else null
}

actual fun readFileContent(path: String): String? {
    return try {
        java.io.File(path).readText()
    } catch (e: Exception) {
        null
    }
}
