package com.dnd.helper.presentation.desktop

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnd.helper.domain.common.Result
import com.dnd.helper.domain.model.MusicTrack
import com.dnd.helper.domain.music.AudioPlayer
import com.dnd.helper.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

data class MusicState(
    val tracks: List<MusicTrack> = emptyList(),
    val isLoading: Boolean = false,
    val currentTrackId: String? = null,
    val isPlaying: Boolean = false,
    val isRepeating: Boolean = false,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val error: String? = null
)

class MusicViewModel(
    private val repository: CharacterRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _state = MutableStateFlow(MusicState())
    val state: StateFlow<MusicState> = _state.asStateFlow()

    private var progressJob: kotlinx.coroutines.Job? = null
    private var lastSeekTime = TimeSource.Monotonic.markNow()

    init {
        refreshMusic()

        // Listen for remote updates via WebSocket
        viewModelScope.launch {
            repository.remoteUpdates.collect { updateType ->
                if (updateType == "music") {
                    println("[Music] Remote update received via WebSocket, reloading tracks...")
                    refreshMusic()
                }
            }
        }
    }

    private fun startProgressPolling() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                // Ignore polling for 1 second after a seek to prevent UI flickering
                if (lastSeekTime.elapsedNow().inWholeMilliseconds > 1000) {
                    if (_state.value.isPlaying) {
                        val currentPos = audioPlayer.getCurrentPosition()
                        val duration = audioPlayer.getDuration()
                        
                        // Improved end detection: 
                        // Only stop if we have a duration and we've reached it.
                        // Desktop streaming doesn't give a reliable "finished" signal,
                        // so we rely on position vs duration.
                        val isAtEnd = duration > 0 && currentPos >= duration - 500
                        
                        if (isAtEnd) {
                            // Song naturally finished
                            if (_state.value.isRepeating) {
                                val trackId = _state.value.currentTrackId
                                val track = _state.value.tracks.find { it.id == trackId }
                                if (track != null) {
                                    audioPlayer.stop()
                                    track.downloadUrl?.let { audioPlayer.play(it) }
                                    _state.value = _state.value.copy(
                                        currentPosition = 0,
                                        isPlaying = true
                                    )
                                } else {
                                    stop()
                                }
                            } else {
                                stop()
                            }
                        } else {
                            _state.value = _state.value.copy(
                                currentPosition = currentPos,
                                duration = duration
                            )
                        }
                    } else {
                        // Even if not playing, update duration if it was 0
                        if (_state.value.duration <= 0) {
                            val duration = audioPlayer.getDuration()
                            if (duration > 0) {
                                _state.value = _state.value.copy(duration = duration)
                            }
                        }
                    }
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    fun refreshMusic() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = repository.getMusic(forceRefresh = true)) {
                is Result.Success -> {
                    _state.value = _state.value.copy(tracks = result.data, isLoading = false)
                }
                is Result.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.error.toString())
                }
            }
        }
    }

    fun togglePlay(track: MusicTrack) {
        if (_state.value.currentTrackId == track.id) {
            if (_state.value.isPlaying) {
                audioPlayer.pause()
                _state.value = _state.value.copy(isPlaying = false)
            } else {
                audioPlayer.resume()
                _state.value = _state.value.copy(isPlaying = true)
                startProgressPolling()
            }
        } else {
            audioPlayer.stop()
            track.downloadUrl?.let { url ->
                audioPlayer.play(url)
                _state.value = _state.value.copy(
                    currentTrackId = track.id,
                    isPlaying = true,
                    currentPosition = 0,
                    duration = 0
                )
                startProgressPolling()
            }
        }
    }

    fun toggleRepeat() {
        _state.value = _state.value.copy(isRepeating = !_state.value.isRepeating)
    }

    fun seekTo(position: Long) {
        lastSeekTime = TimeSource.Monotonic.markNow()
        audioPlayer.seekTo(position)
        _state.value = _state.value.copy(currentPosition = position)
    }

    fun stop() {
        audioPlayer.stop()
        progressJob?.cancel()
        _state.value = _state.value.copy(isPlaying = false, currentTrackId = null, currentPosition = 0)
    }

    fun addTrack(name: String, url: String) {
        viewModelScope.launch {
            val track = MusicTrack(
                id = "music-${kotlin.random.Random.nextInt(100000)}",
                name = name,
                url = url
            )
            val result = repository.saveMusic(track)
            if (result is Result.Success) {
                refreshMusic()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
