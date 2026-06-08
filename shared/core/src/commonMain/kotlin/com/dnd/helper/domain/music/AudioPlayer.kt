package com.dnd.helper.domain.music

interface AudioPlayer {
    fun play(url: String)
    fun pause()
    fun resume()
    fun stop()
    fun setVolume(volume: Float)
    fun isPlaying(): Boolean
    fun seekTo(position: Long)
    fun getCurrentPosition(): Long
    fun getDuration(): Long
}
