package com.colux.libretune.ui.discography

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.colux.libretune.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiscographyViewModel @Inject constructor(
    private val repository: MusicRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // ... Fetch artistDetails similar to ArtistDetailViewModel ...
    // ... Add state for the selected filter (Album or Singles) ...
}