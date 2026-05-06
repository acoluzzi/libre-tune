package com.colux.libretune.desktop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.SongRepository
import com.colux.libretune.desktop.DesktopAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.logging.Logger

sealed interface PlayerState {
    data object Idle : PlayerState
    data class Loading(val song: Song) : PlayerState
    data class Playing(val song: Song, val paused: Boolean = false) : PlayerState
    data class Error(val song: Song, val message: String) : PlayerState
}

class DesktopPlayerViewModel(
    private val songRepository: SongRepository,
) : ViewModel() {

    private val logger = Logger.getLogger(DesktopPlayerViewModel::class.java.name)
    private val player = DesktopAudioPlayer()

    private val _state = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    fun playSong(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            logger.info("Fetching stream URL for song: ${song.title} (id=${song.id})")
            _state.value = PlayerState.Loading(song)
            val url = songRepository.getSongUrlById(song.id)
            if (url == null) {
                logger.severe("Stream URL is null for song: ${song.id}")
                _state.value = PlayerState.Error(song, "Could not retrieve stream URL")
                return@launch
            }
            logger.info("Got stream URL, starting playback: $url")
            player.play(url)
            _state.value = PlayerState.Playing(song, paused = false)
        }
    }

    fun togglePause() {
        val current = _state.value as? PlayerState.Playing ?: return
        if (current.paused) {
            player.resume()
            _state.value = current.copy(paused = false)
        } else {
            player.pause()
            _state.value = current.copy(paused = true)
        }
    }

    fun stop() {
        player.stop()
        _state.value = PlayerState.Idle
    }

    override fun onCleared() {
        player.stop()
        super.onCleared()
    }
}
