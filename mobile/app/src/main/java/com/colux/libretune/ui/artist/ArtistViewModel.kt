package com.colux.libretune.ui.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.repository.ArtistRepository
import com.colux.libretune.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistViewModel @Inject constructor(
    private val repository: ArtistRepository,
    private val songRepository: SongRepository,
    savedStateHandle: SavedStateHandle // Hilt provides this to access navigation arguments
) : ViewModel() {

    private val artistId: String = savedStateHandle.get<String>("artistId")!!

    // The entire state of the screen can be represented by a single StateFlow.
    val uiState: StateFlow<ArtistUiState> =
        repository.getArtistDetails(artistId)
            .map { details ->
                if (details != null) {
                    ArtistUiState.Success(details)
                } else {
                    ArtistUiState.Loading
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ArtistUiState.Loading
            )

    val isArtistSaved = repository.isArtistSaved(artistId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun likeArtist() {
        viewModelScope.launch {
            repository.saveArtist(artistId)
        }
    }

    fun dislikeArtist() {
        viewModelScope.launch {
            repository.unsaveArtist(artistId)
        }
    }

    init {
        // Trigger a refresh when the ViewModel is created
        viewModelScope.launch {
            repository.refreshArtistDetails(artistId)
        }
    }
}

// A sealed interface to represent the different states of your screen's UI
sealed interface ArtistUiState {
    data object Loading : ArtistUiState
    data class Success(val details: ArtistDetails) : ArtistUiState
    data class Error(val message: String) : ArtistUiState
}