package com.colux.libretune.desktop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.wrapper.PlaylistWithSongs
import com.colux.libretune.data.repository.ArtistRepository
import com.colux.libretune.data.repository.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LibraryViewModel(
    playlistRepository: PlaylistRepository,
    artistRepository: ArtistRepository,
) : ViewModel() {

    val playlists: StateFlow<List<PlaylistWithSongs>> =
        playlistRepository.getSavedPlaylistsWithSongs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val artists: StateFlow<List<Artist>> =
        artistRepository.getSavedArtists()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
