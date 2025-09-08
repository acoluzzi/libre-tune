package com.colux.libretune.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.local.dao.HistoryDao
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: ArtistRepository,
    private val historyDao: HistoryDao
) :
    ViewModel() {

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed

    private val _madeForYou = MutableStateFlow<List<Song>>(emptyList())
    val madeForYou: StateFlow<List<Song>> = _madeForYou


    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()


    init {
        viewModelScope.launch {
            // 1. Get top artists from history to use as a seed.
            //    val seedArtists = historyDao.getTopArtists() // You would need to create this query
            // 2. Pass seed artists to repository to get recommendations
            // val recommendations = musicRepository.getRecommendations(seedArtists)
            // 3. Update the UI state with the results
        }
    }


    private fun fetchSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLoading.value = false
        }
    }
}