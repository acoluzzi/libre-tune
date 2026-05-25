package com.colux.libretune.ui.create_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.remote.auth.YtMusicAuthRepository
import com.colux.libretune.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNewPlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    authRepository: YtMusicAuthRepository,
) : ViewModel() {

    val isSignedInToYouTubeMusic: StateFlow<Boolean> = authRepository.state
        .map { it != null }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            authRepository.current() != null
        )

    fun createPlaylist(name: String, syncWithYouTubeMusic: Boolean) {
        viewModelScope.launch {
            playlistRepository.createNewPlaylist(
                name = name,
                syncWithYouTubeMusic = syncWithYouTubeMusic
            )
        }
    }
}
