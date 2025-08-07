package com.colux.libretune.data.repository.tube

import android.util.Log
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class YouTubeExtractionRepository @Inject constructor() : MusicRepository {

    init {
        NewPipe.init(DownloaderImpl.init(null), Localization("en", "US"))
    }

    override suspend fun getSongById(id: String): Song? {
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
                Song(
                    id = id,
                    title = extractor.name,
                    artist = extractor.uploaderName,
                    imageUrl = extractor.thumbnails.first().url,
                    mediaUrl = bestAudioStreamUrl
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun getSongs(): List<Song> {
        return withContext(Dispatchers.IO) {
            val song = getSongById("dQw4w9WgXcQ") // Example video ID
            if (song != null) {
                listOf(song)
            } else {
                emptyList()
            }
        }
    }

    override suspend fun searchSongs(query: String): List<Song> {
        return withContext(Dispatchers.IO) {
            Log.d("YouTubeExtractionRepository", "Searching for: $query")
            try {
                // 1. Use the standard YouTube service.
                val service = NewPipe.getService(ServiceList.YouTube.serviceId)

                val searchExtractor = service.getSearchExtractor(
                    query,
                    listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS),
                    ""
                )

                searchExtractor.fetchPage()

                searchExtractor.fetchPage()
                val searchResults = searchExtractor.initialPage.items

                Log.d(
                    "YouTubeExtractionRepository",
                    "Search results: ${searchResults.size}"
                )

                searchResults.mapNotNull { it as? StreamInfoItem }.map {
                    Song(
                        id = it.url.substringAfter("?v="),
                        title = it.name,
                        artist = it.uploaderName,
                        imageUrl = it.thumbnails.first().url,
                        mediaUrl = null
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
}