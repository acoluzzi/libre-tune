package com.colux.libretune.ui.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val repository: MusicRepository,
    savedStateHandle: SavedStateHandle // Hilt provides this to access navigation arguments
) : ViewModel() {

    private val artistId: String = savedStateHandle.get<String>("artistId")!!

    // The entire state of the screen can be represented by a single StateFlow.
    val uiState: StateFlow<ArtistUiState> =
        // Start with the flow from the repository
        repository.getArtistDetails(artistId)
            .map { details ->
                // Map the result from the repository into our UI state object
                if (details != null) {
                    ArtistUiState.Success(details)
                } else {
                    // This case can be hit if the initial DB query is empty
                    ArtistUiState.Loading
                }
            }
            .stateIn(
                scope = viewModelScope,
                // This is a robust policy that keeps the data for 5 seconds
                // after the UI goes away, preventing re-fetching on rotation.
                started = SharingStarted.WhileSubscribed(5000),
                // The initial state while the flow is starting up.
                initialValue = ArtistUiState.Loading
            )
}

// A sealed interface to represent the different states of your screen's UI
sealed interface ArtistUiState {
    data object Loading : ArtistUiState
    data class Success(val details: ArtistDetails) : ArtistUiState
    data class Error(val message: String) : ArtistUiState
}