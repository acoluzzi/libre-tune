package com.colux.libretune.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val repository: PlaylistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val logger = java.util.logging.Logger.getLogger("PlaylistDetailViewModel")
    private val playlistId: String = savedStateHandle.get<String>("playlistId")!!

    val isPlaylistSaved = repository.isPlaylistSaved(playlistId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Set to true once the initial refresh attempt completes (success or failure).
    private val _refreshDone = MutableStateFlow(false)

    val uiState: StateFlow<PlaylistUiState> =
        combine(repository.getPlaylistDetails(playlistId), _refreshDone) { details, refreshDone ->
            logger.info { "Emitting state: details=$details refreshDone=$refreshDone" }
            when {
                details != null -> PlaylistUiState.Success(details)
                refreshDone -> PlaylistUiState.Error("Could not load album. Check your connection and try again.")
                else -> PlaylistUiState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlaylistUiState.Loading
        )

    init {
        viewModelScope.launch {
            repository.refreshPlaylistDetails(playlistId)
            // Mark refresh as done so the UI can switch from Loading → Error if DB is still empty.
            _refreshDone.value = true
        }
    }


    fun likePlaylist() {
        viewModelScope.launch {
            repository.savePlaylist(playlistId)
            logger.info("Added playlist $playlistId from library")
        }
    }

    fun dislikePlaylist() {
        viewModelScope.launch {
            repository.unsavePlaylist(playlistId)
            logger.info("Removed playlist $playlistId from library")
        }
    }


}

sealed interface PlaylistUiState {
    data object Loading : PlaylistUiState
    data class Success(val details: PlaylistDetails) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
}