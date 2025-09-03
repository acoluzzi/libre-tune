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

    // The entire state of the screen can be represented by a single StateFlow.
    val uiState: StateFlow<PlaylistUiState> =
        // Start with the flow from the repository
        repository.getPlaylistDetails(playlistId)
            .map { details ->
                logger.info { "Received data from repository: $details" }
                // Map the result from the repository into our UI state object
                if (details != null) {
                    PlaylistUiState.Success(details)
                } else {
                    // This case can be hit if the initial DB query is empty
                    PlaylistUiState.Loading
                }
            }
            .stateIn(
                scope = viewModelScope,
                // This is a robust policy that keeps the data for 5 seconds
                // after the UI goes away, preventing re-fetching on rotation.
                started = SharingStarted.WhileSubscribed(5000),
                // The initial state while the flow is starting up.
                initialValue = PlaylistUiState.Loading
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
    data class Success(val details: PlaylistDetails) : PlaylistUiState
    data class Error(val message: String) : PlaylistUiState
}