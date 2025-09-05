package com.colux.libretune.data.remote.tube

import android.util.Log
import com.colux.libretune.data.model.ArtistDetails
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.SearchSuggestion
import com.colux.libretune.data.remote.tube.mapper.toAlbum
import com.colux.libretune.data.remote.tube.mapper.toArtist
import com.colux.libretune.data.remote.tube.mapper.toDataModel
import com.colux.libretune.data.remote.tube.mapper.toPlaylist
import com.colux.libretune.data.remote.tube.mapper.toPlaylists
import com.colux.libretune.data.remote.tube.mapper.toSong
import com.coluzziandrea.libretune_extractor.LibreTuneExtractor
import com.coluzziandrea.libretune_extractor.model.GenericMusicItem
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

    suspend fun searchSuggestions(query: String): List<SearchSuggestion> {
        return withContext(Dispatchers.IO) {
            try {
                return@withContext libreTuneExtractor.searchSuggestions(query).mapNotNull {
                    if (it.suggestion != null && it.suggestion?.isNotEmpty() == true) {
                        com.colux.libretune.data.model.SearchSuggestion.QuerySuggestion(
                            it.suggestion!!,
                            false
                        )
                    } else if (it.musicItem != null) {
                        val musicItem = it.musicItem
                        when (musicItem) {
                            is GenericMusicItem.SongResult -> SearchSuggestion.EntitySuggestion(
                                song = musicItem.toSong(), type = "Song"
                            )

                            is GenericMusicItem.ArtistResult -> SearchSuggestion.EntitySuggestion(
                                artist = musicItem.toArtist(),
                                type = "Artist"
                            )

                            is GenericMusicItem.AlbumResult -> SearchSuggestion.EntitySuggestion(
                                album = musicItem.toAlbum(),
                                type = "Album"
                            )

                            is GenericMusicItem.PlaylistResult -> SearchSuggestion.EntitySuggestion(
                                playlist = musicItem.toPlaylist(),
                                type = "Playlist"
                            )

                            else -> null
                        }

                    } else {
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
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
                        return@withContext it.toDataModel()
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

    suspend fun discography(id: String, param: String? = null): List<Playlist> {
        return withContext(Dispatchers.IO) {
            try {
                libreTuneExtractor.discography(id, param).let {
                    if (it != null) {
                        return@withContext it.toPlaylists()
                    } else {
                        emptyList()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    suspend fun getPlaylistDetails(id: String): PlaylistDetails? {
        return withContext(Dispatchers.IO) {
            try {
                libreTuneExtractor.playlist(id).let {
                    if (it != null) {
                        Log.d(
                            "YouTubeExtractionRepository",
                            "Scraped playlist: ${it.name}"
                        )
                        return@withContext it.toDataModel()
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


}