package com.colux.libretune.data.repository

import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.SearchResult

interface MusicRepository {

    suspend fun getSongUrlById(id: String): String?

    suspend fun searchContent(query: String): List<SearchResult>

    suspend fun getArtistDetails(id: String): ArtistDetails?

    suspend fun getPlaylistDetails(id: String): PlaylistDetails?
}