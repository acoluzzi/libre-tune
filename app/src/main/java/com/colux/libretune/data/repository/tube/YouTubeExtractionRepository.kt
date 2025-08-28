package com.colux.libretune.data.repository.tube

import android.util.Log
import com.colux.libretune.data.model.Album
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.Song
import com.colux.libretune.data.repository.MusicRepository
import com.coluzziandrea.libretune_extractor.LibreTuneExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.channel.ChannelInfoItem
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class YouTubeExtractionRepository @Inject constructor(
    val libreTuneExtractor: LibreTuneExtractor
) : MusicRepository {

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

    override suspend fun searchContent(query: String): List<SearchResult> {
        // We use coroutineScope to run both searches concurrently for better performance.
        return coroutineScope {
            // Start the artist search in the background
            val artistsDeferred = async(Dispatchers.IO) {
                performSearch(query, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_ARTISTS))
            }

            // Start the song search in the background
            val songsDeferred = async(Dispatchers.IO) {
                performSearch(query, listOf(YoutubeSearchQueryHandlerFactory.MUSIC_SONGS))
            }

            // Wait for both searches to complete
            val artistResults = artistsDeferred.await()
            val songResults = songsDeferred.await()

            // Combine the lists: top 3 artists first, then all the songs
            val combinedList = mutableListOf<SearchResult>()
            combinedList.addAll(artistResults.take(2))
            combinedList.addAll(songResults)

            combinedList
        }
    }

    override suspend fun getArtistDetails(id: String): ArtistDetails? {
        return withContext(Dispatchers.IO) {
            try {

                libreTuneExtractor.artist(id).let {
                    if (it != null) {
                        Log.d(
                            "YouTubeExtractionRepository",
                            "Scraped artist: ${it.name}, Top songs: ${it.topSongs.size}"
                        )
                        return@withContext ArtistDetails(
                            name = it.name,
                            avatarUrl = null,
                            description = it.description,
                            bannerUrl = it.images.firstOrNull()?.url,
                            topSongs = it.topSongs.map { song ->
                                Song(
                                    id = song.id,
                                    title = song.title,
                                    artist = it.name,
                                    imageUrl = song.images.firstOrNull()?.url ?: "",
                                    mediaUrl = null
                                )
                            },
                            albums = it.albums.map {
                                Album(
                                    id = it.id,
                                    name = it.name,
                                    thumbnailUrl = it.thumbnailUrl
                                )
                            },
                            similarArtists = emptyList()
                        )
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

    override suspend fun getPlaylistDetails(id: String): PlaylistDetails? {

        return withContext(Dispatchers.IO) {
            try {
                libreTuneExtractor.playlist(id).let {
                    if (it != null) {
                        Log.d(
                            "YouTubeExtractionRepository",
                            "Scraped playlist: ${it.name}"
                        )
                        return@withContext PlaylistDetails(
                            name = it.name,
                            artist = it.artist,
                            bannerUrl = it.images.maxByOrNull { image ->
                                image.width
                            }?.url,
                            songs = it.songs.map { song ->
                                Song(
                                    id = song.id,
                                    title = song.title,
                                    artist = it.name,
                                    imageUrl = song.images.firstOrNull()?.url ?: "",
                                    mediaUrl = null
                                )
                            },
                            relatedPlaylists = it.relatedPlaylists.map {
                                Playlist(
                                    id = it.id,
                                    name = it.name,
                                    thumbnailUrl = it.thumbnailUrl
                                )
                            },
                        )
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

    /**
     * A helper function to perform a search with a specific filter.
     */
    private fun performSearch(query: String, contentFilter: List<String>): List<SearchResult> {
        return try {
            val service = NewPipe.getService(ServiceList.YouTube.serviceId)
            val searchExtractor = service.getSearchExtractor(query, contentFilter, "")
            searchExtractor.fetchPage()

            val searchResults: List<InfoItem> = searchExtractor.initialPage.items

            searchResults.mapNotNull { item ->
                when (item) {
                    is StreamInfoItem -> SearchResult.SongResult(
                        Song(
                            id = item.url.substringAfter("?v="),
                            title = item.name,
                            artist = item.uploaderName,
                            imageUrl = item.thumbnails.first().url,
                            mediaUrl = null
                        )
                    )

                    is ChannelInfoItem -> SearchResult.ArtistResult(
                        Artist(
                            id = item.url.substringAfter("/channel/"),
                            name = item.name,
                            imageUrl = item.thumbnails.first().url
                        )
                    )

                    else -> null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}