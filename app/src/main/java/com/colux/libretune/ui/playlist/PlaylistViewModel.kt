package com.colux.libretune.ui.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    private val repository: PlaylistRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val playlistId: String = savedStateHandle.get<String>("playlistId")!!

    private val _playlistDetails = MutableStateFlow<PlaylistDetails?>(null)
    val playlistDetails = _playlistDetails.asStateFlow()

    init {
        viewModelScope.launch {
            _playlistDetails.value = repository.getPlaylistDetails(playlistId)
        }
    }
}