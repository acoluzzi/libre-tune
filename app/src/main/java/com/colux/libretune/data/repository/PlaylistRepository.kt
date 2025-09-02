package com.colux.libretune.data.repository

import com.colux.libretune.data.local.dao.AlbumDao
import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepository @Inject constructor(
    private val remote: YouTubeExtractionRepository,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao
) {


    suspend fun getPlaylistDetails(id: String): PlaylistDetails? {
        return remote.getPlaylistDetails(id)
    }
}