package com.colux.libretune.ui.create_playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNewPlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createNewPlaylist(name)
        }
    }
}