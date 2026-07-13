package com.dnd.helper.fakes

import com.dnd.helper.domain.music.AudioPlayer

class FakeAudioPlayer : AudioPlayer {
    var isPlayingState = false
    var isPaused = false
    var currentPositionValue = 0L
    var durationValue = 0L
    var lastSeekPosition = 0L
    var lastPlayedUrl: String? = null
    var stopCalled = false
    var resumeCalled = false
    var volumeValue = 1.0f

    override fun play(url: String) {
        lastPlayedUrl = url
        isPlayingState = true
        isPaused = false
        currentPositionValue = 0L
    }

    override fun pause() {
        isPaused = true
        isPlayingState = false
    }

    override fun resume() {
        if (isPaused) {
            isPaused = false
            isPlayingState = true
            resumeCalled = true
        }
    }

    override fun stop() {
        isPlayingState = false
        isPaused = false
        currentPositionValue = 0L
        stopCalled = true
    }

    override fun setVolume(volume: Float) {
        this.volumeValue = volume
    }

    override fun isPlaying(): Boolean = isPlayingState

    override fun seekTo(position: Long) {
        lastSeekPosition = position
        currentPositionValue = position
    }

    override fun getCurrentPosition(): Long = currentPositionValue

    override fun getDuration(): Long = durationValue

    fun reset() {
        isPlayingState = false
        isPaused = false
        currentPositionValue = 0L
        durationValue = 0L
        lastSeekPosition = 0L
        lastPlayedUrl = null
        stopCalled = false
        resumeCalled = false
        volumeValue = 1.0f
    }
}
