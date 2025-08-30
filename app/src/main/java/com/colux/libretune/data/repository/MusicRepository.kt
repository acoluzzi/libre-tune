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

    fun getArtistDetails(artistId: String): Flow<ArtistDetails?> {
        return artistDao.getArtistWithContent(artistId)
            .map { databaseModel ->
                // The Flow is now of type ArtistDetails?, which the ViewModel expects.
                databaseModel?.toArtistDetails()
            }
            .onStart {
                updateArtistDetailsFromRemote(artistId)
            }

    }


    suspend fun getPlaylistDetails(id: String): PlaylistDetails? {
        return remote.getPlaylistDetails(id)
    }


    private suspend fun updateArtistDetailsFromRemote(artistId: String) {
//        try {
//            // 3. Fetch fresh data from the remote source (your scraper).
//            val remoteArtistDetails = remote.scrapeArtistPage(artistId)
//
//            // 4. If the fetch was successful, save the new data to the database.
//            if (remoteArtistDetails != null) {
//                // You would have DAO methods to insert/update artists, songs, albums, etc.
//                artistDao.insertOrUpdate(remoteArtistDetails)
//            }
//        } catch (e: Exception) {
//            // Handle network errors, etc.
//            e.printStackTrace()
//        }
        TODO()
    }
}