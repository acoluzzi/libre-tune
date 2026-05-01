package com.colux.libretune.ui.discography

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscographyViewModel @Inject constructor(
    private val repository: AlbumRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val artistId: String = savedStateHandle.get<String>("artistId")!!

    val uiState: StateFlow<DiscographyUiState> =
        repository.getArtistDiscography(artistId)
            .map { list ->
                DiscographyUiState.Success(list)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DiscographyUiState.Loading
            )

    init {
        viewModelScope.launch {
            repository.refreshArtistDiscography(artistId)
        }
    }
}


sealed interface DiscographyUiState {
    data object Loading : DiscographyUiState
    data class Success(val albums: List<Playlist>) : DiscographyUiState
    data class Error(val message: String) : DiscographyUiState
}