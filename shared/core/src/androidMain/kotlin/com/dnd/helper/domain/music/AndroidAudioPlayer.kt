package com.dnd.helper.domain.music

import android.media.MediaPlayer

class AndroidAudioPlayer : AudioPlayer {
    private var mediaPlayer: MediaPlayer? = null

    override fun play(url: String) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { start() }
        }
    }

    override fun pause() {
        mediaPlayer?.pause()
    }

    override fun resume() {
        mediaPlayer?.start()
    }

    override fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    override fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
    }

    override fun getCurrentPosition(): Long = mediaPlayer?.currentPosition?.toLong() ?: 0L

    override fun getDuration(): Long = mediaPlayer?.duration?.toLong() ?: 0L
}
