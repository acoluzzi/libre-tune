package com.colux.libretune.data.repository

import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepository @Inject constructor(
    private val remote: YouTubeExtractionRepository,
) {
    suspend fun getSongUrlById(id: String): String? {
        return remote.getSongUrlById(id)
    }
}