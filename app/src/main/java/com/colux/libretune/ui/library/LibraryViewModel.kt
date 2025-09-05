package com.colux.libretune.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.wrapper.PlaylistWithSongs
import com.colux.libretune.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(val playlistRepository: PlaylistRepository) :
    ViewModel() {


    // This flow now gets ALL playlists with their associated songs.
    val playlists: StateFlow<List<PlaylistWithSongs>> =
        playlistRepository.getLocalPlaylistsWithSongs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Creates and inserts a new, empty, user-created playlist.
     */
    fun createNewPlaylist(name: String) {
        viewModelScope.launch {
            playlistRepository.createNewPlaylist(name)
        }
    }
}