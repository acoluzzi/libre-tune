package com.colux.libretune.data.repository

import com.colux.libretune.data.model.Song

interface MusicRepository {

    suspend fun getSongById(id: String): Song?

    suspend fun getSongs(): List<Song>

    suspend fun searchSongs(query: String): List<Song>
}