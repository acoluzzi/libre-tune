package com.colux.libretune.ui.discography

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscographyViewModel @Inject constructor(
    private val repository: MusicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val discographyId: String = savedStateHandle.get<String>("discographyId")!!

    private val _albums = MutableStateFlow<List<Playlist>>(listOf())
    val albums = _albums.asStateFlow()

    private val _singlesEp = MutableStateFlow<List<Playlist>>(listOf())
    val singlesEp = _singlesEp.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchDetails()
    }

    private fun fetchDetails() {
        viewModelScope.launch {
            _isLoading.value = true
//            val (albumsRes, singlesEpRes) = repository.getArtistDiscography(discographyId)
//            _albums.value = albumsRes
//            _singlesEp.value = singlesEpRes
            _isLoading.value = false
        }
    }
}