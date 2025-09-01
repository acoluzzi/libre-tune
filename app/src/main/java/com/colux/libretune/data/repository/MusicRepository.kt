package com.colux.libretune.data.repository

import com.colux.libretune.data.local.dao.ArtistDao
import com.colux.libretune.data.mapper.toArtistDetails
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val remote: YouTubeExtractionRepository,
    private val artistDao: ArtistDao
) {

    suspend fun getSongUrlById(id: String): String? {
        return remote.getSongUrlById(id)
    }

    suspend fun searchContent(query: String): List<SearchResult> {
        return remote.searchContent(query)
    }


    suspend fun getPlaylistDetails(id: String): PlaylistDetails? {
        return remote.getPlaylistDetails(id)
    }

    // Define how long the cache should be valid (e.g., 60 minutes)
    private val cacheTtlMillis = TimeUnit.MINUTES.toMillis(60)

    fun getArtistDetails(artistId: String): Flow<ArtistDetails?> {
        return artistDao.getArtistWithContent(artistId)
            .map { it?.toArtistDetails() }
            .onStart {
                // Check if we need to fetch before emitting the cached data
                if (shouldFetch(artistId)) {
                    updateArtistDetailsFromRemote(artistId)
                }
            }
    }

    private suspend fun shouldFetch(artistId: String): Boolean {
        // Fetch the artist record just to check its timestamp
        val cachedArtist = artistDao.getArtist(artistId)

        // Always fetch if there's no data
        if (cachedArtist == null) return true

        // Fetch if the data is older than our TTL
        val isStale =
            (System.currentTimeMillis() - cachedArtist.updateTimestamp) > cacheTtlMillis
        return isStale
    }

    private suspend fun updateArtistDetailsFromRemote(artistId: String) {
//        try {
//            val remoteArtistDetails = remoteSource.scrapeArtistPage(artistId)
//            if (remoteArtistDetails != null) {
//                // When mapping, include the current timestamp
//                val artistEntity = ArtistEntity(
//                    artistId = artistId,
//                    name = remoteArtistDetails.name,
//                    imageUrl = remoteArtistDetails.imageUrl,
//                    bannerUrl = remoteArtistDetails.bannerUrl,
//                    lastFetchedTimestamp = System.currentTimeMillis() // Set the timestamp
//                )
//                // You would update your mapper to handle this and save all related data
//                artistDao.insertArtist(artistEntity)
//                // ... save songs, albums, etc.
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
        TODO()
    }


}