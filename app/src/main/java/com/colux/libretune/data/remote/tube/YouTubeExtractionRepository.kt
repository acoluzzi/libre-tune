package com.colux.libretune.data.remote.tube

import android.util.Log
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.remote.tube.mapper.toDataModel
import com.coluzziandrea.libretune_extractor.LibreTuneExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.Localization
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class YouTubeExtractionRepository @Inject constructor(
    val libreTuneExtractor: LibreTuneExtractor
) {

    init {
        NewPipe.init(DownloaderImpl.init(null), Localization("en", "US"))
    }

    suspend fun getSongUrlById(id: String): String? {
        return withContext(Dispatchers.IO) {
            Log.d("YouTubeExtractionRepository", "Fetching song with ID: $id")
            try {
                // 1. Get the stream extractor from the YouTube service
                val service = NewPipe.getService(ServiceList.YouTube.serviceId)
                val extractor =
                    service.getStreamExtractor("https://www.youtube.com/watch?v=$id")

                // 2. Fetch the page data
                extractor.fetchPage()

                // 3. Find the best audio stream URL
                val bestAudioStreamUrl = extractor.audioStreams
                    .filter { it.format?.name?.contains("M4A", ignoreCase = true) ?: false }
                    .maxByOrNull { it.averageBitrate }
                    ?.url

                Log.d("YouTubeExtractionRepository", "Best audio stream URL: $bestAudioStreamUrl")

                // If we can't get a playable URL, we can't proceed.
                if (bestAudioStreamUrl == null) {
                    return@withContext null
                }

                // 4. Extract metadata and build the Song object
                bestAudioStreamUrl
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    suspend fun searchContent(query: String): SearchResult? {
        return coroutineScope {
            try {
                return@coroutineScope libreTuneExtractor.search(query)?.toDataModel()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun getArtistDetails(id: String): ArtistDetails? {
        return withContext(Dispatchers.IO) {
            try {

                libreTuneExtractor.artist(id).let {
                    if (it != null) {
                        Log.d(
                            "YouTubeExtractionRepository",
                            "Scraped artist: ${it.name}, Top songs: ${it.topSongs.size}"
                        )
                        return@withContext ArtistDetails.from(it)
                    } else {
                        null
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun getArtistItemContinuation(id: String): List<Playlist> {
        TODO("Not yet implemented")
    }

    suspend fun getPlaylistDetails(id: String): PlaylistDetails? {
        TODO()
//        return withContext(Dispatchers.IO) {
//            try {
//                libreTuneExtractor.playlist(id).let {
//                    if (it != null) {
//                        Log.d(
//                            "YouTubeExtractionRepository",
//                            "Scraped playlist: ${it.name}"
//                        )
//                        return@withContext PlaylistDetails.from(it)
//                    } else {
//                        null
//                    }
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
    }

    /**
     * A helper function to perform a search with a specific filter.
     */
    private fun performSearch(query: String, contentFilter: List<String>): List<SearchResult> {
        TODO()

//        return try {
//            val service = NewPipe.getService(ServiceList.YouTube.serviceId)
//            val searchExtractor = service.getSearchExtractor(query, contentFilter, "")
//            searchExtractor.fetchPage()
//
//            val searchResults: List<InfoItem> = searchExtractor.initialPage.items
//
//            searchResults.mapNotNull { item ->
//                when (item) {
//                    is StreamInfoItem -> SearchResult.SongResult(
//                        Song(
//                            id = item.url.substringAfter("?v="),
//                            title = item.name,
//                            artists = listOf(
//                                Artist(
//                                    id = "unknown",
//                                    name = item.uploaderName ?: "unknown",
//                                    imageUrl = null
//                                )
//                            ),
//                            images = item.thumbnails.map {
//                                Image(
//                                    url = it.url,
//                                    width = it.width,
//                                    height = it.height
//                                )
//                            }
//                        )
//                    )
//
//                    is ChannelInfoItem -> SearchResult.ArtistResult(
//                        Artist(
//                            id = item.url.substringAfter("/channel/"),
//                            name = item.name,
//                            imageUrl = item.thumbnails.first().url
//                        )
//                    )
//
//                    else -> null
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            emptyList()
//        }
    }

}