package com.colux.libretune.data.repository

import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.Song

interface MusicRepository {

    suspend fun getSongById(id: String): Song?

    suspend fun getSongs(): List<Song>

    suspend fun searchContent(query: String): List<SearchResult>

    suspend fun getArtistDetails(id: String): ArtistDetails?
}