package com.colux.libretune.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.local.LikedSongDao
import com.colux.libretune.data.local.toSong
import com.colux.libretune.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(private val likedSongDao: LikedSongDao) : ViewModel() {


    val likedSongs: StateFlow<List<Song>> = likedSongDao.getLikedSongs().map { list ->
        list.map { it.toSong() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}