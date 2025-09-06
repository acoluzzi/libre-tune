package com.colux.libretune.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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

    val uiState: StateFlow<PlaylistUiState> =
        repository.getPlaylistDetails(playlistId)
            .map { details ->
                PlaylistUiState.Success(details)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = PlaylistUiState.Loading // This is the ONLY time we use Loading
            )

    init {
        // Trigger a refresh when the ViewModel is created
        viewModelScope.launch {
            repository.refreshPlaylistDetails(playlistId)
        }
    }


}

sealed interface PlaylistUiState {
    data object Loading : PlaylistUiState
    data class Success(val details: PlaylistDetails?) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
}