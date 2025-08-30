package com.colux.libretune.ui.library

import androidx.lifecycle.ViewModel
import com.colux.libretune.data.local.dao.SongDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(private val likedSongDao: SongDao) : ViewModel() {


//    val likedSongs: StateFlow<List<Song>> = likedSongDao.getLikedSongs().map { list ->
//        list.map { it.toSong() }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}