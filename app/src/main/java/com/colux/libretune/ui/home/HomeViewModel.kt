package com.colux.libretune.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val musicRepository: MusicRepository) :
    ViewModel() {

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed

    private val _madeForYou = MutableStateFlow<List<Song>>(emptyList())
    val madeForYou: StateFlow<List<Song>> = _madeForYou


    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()


    init {
        fetchSongs()
    }


    private fun fetchSongs() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLoading.value = false
        }
    }
}