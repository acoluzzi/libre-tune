package com.colux.libretune.ui.add_to_playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.model.wrapper.PlaylistWithSongs
import com.colux.libretune.data.repository.PlaylistRepository
import com.colux.libretune.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddToPlaylistViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val songRepository: SongRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val songId: String = savedStateHandle.get<String>("songId")!!

    val song: StateFlow<Song?> = songRepository.getSongById(songId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val playlists: StateFlow<List<PlaylistForSelection>> =
        playlistRepository.getLocalPlaylistsWithSongs()
            .combine(song) { allPlaylists, currentSong ->
                if (currentSong == null) {
                    emptyList()
                } else {
                    allPlaylists.map { playlist ->
                        val containsSong = playlist.songs.any { it.id == currentSong.id }
                        PlaylistForSelection(playlist, containsSong)
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val isSongInAnyPlaylist: StateFlow<Boolean> = playlists
        .map { list -> list.any { it.containsSong } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    fun toggleSongInPlaylist(playlistId: String, containsSong: Boolean) {

        viewModelScope.launch {
            if (containsSong) {
                songRepository.removeSongFromPlaylist(
                    playlistId = playlistId,
                    songId = song.value?.id ?: return@launch
                )
            } else {
                // Ensure song is in DB before adding to playlist
                songRepository.addSongToPlaylist(
                    playlistId = playlistId,
                    song = song.value ?: return@launch
                )
            }
        }
    }

    fun removeSongFromAllPlaylists() {
        viewModelScope.launch {
            songRepository.removeSongFromAllPlaylists(song.value?.id ?: return@launch)
        }
    }
}

data class PlaylistForSelection(
    val playlist: PlaylistWithSongs,
    val containsSong: Boolean
)
