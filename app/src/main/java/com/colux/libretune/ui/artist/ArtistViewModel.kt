package com.colux.libretune.ui.artist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    private val repository: MusicRepository,
    savedStateHandle: SavedStateHandle // Hilt provides this to access navigation arguments
) : ViewModel() {

    private val artistId: String = savedStateHandle.get<String>("artistId")!!

    private val _artistDetails = MutableStateFlow<ArtistDetails?>(null)
    val artistDetails = _artistDetails.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchDetails()
    }

    private fun fetchDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _artistDetails.value = repository.getArtistDetails(artistId)
            _isLoading.value = false
        }
    }
}