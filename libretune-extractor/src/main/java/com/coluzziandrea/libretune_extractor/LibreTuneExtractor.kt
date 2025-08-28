package com.coluzziandrea.libretune_extractor

import com.coluzziandrea.libretune_extractor.browse_response.BrowseDataFetcher
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.ArtistDetails
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.PlaylistDetails
import com.coluzziandrea.libretune_extractor.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LibreTuneExtractor @Inject constructor(
    private val client: OkHttpClient
) {

    private val browseDataFetcher = BrowseDataFetcher(client)

    suspend fun playlist(playlistId: String): PlaylistDetails? {
        return withContext(Dispatchers.IO) {
            try {
                val browseDataObject = browseDataFetcher.fetchBrowseData("/browse/$playlistId")

                if (browseDataObject == null) {
                    Logger.getLogger("ArtistScraper")
                        .warning("Failed to fetch or parse browse data for playlistId: $playlistId")
                    return@withContext null
                }

                var playlistName = ""
                var artist = ""
                val songs = mutableListOf<Song>()
                val relatedPlaylists = mutableListOf<Playlist>()
                val playlistImages = mutableListOf<Image>()

                val header =
                    browseDataObject.contents.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()

                if (header is SectionContent.MusicResponsiveHeaderContent) {
                    playlistName =
                        header.musicResponsiveHeaderRenderer.title.runs.firstOrNull()?.text ?: ""
                    artist =
                        header.musicResponsiveHeaderRenderer.straplineTextOne.runs.firstOrNull()?.text
                            ?: ""

                    header.musicResponsiveHeaderRenderer.thumbnail.musicThumbnailRenderer.thumbnail.thumbnails.forEach {
                        playlistImages.add(
                            Image(
                                url = it.url,
                                width = it.width,
                                height = it.height
                            )
                        )
                    }

                }

                browseDataObject.contents.twoColumnBrowseResultsRenderer?.secondaryContents?.sectionListRenderer?.contents?.forEach { content ->
                    if (content is SectionContent.MusicShelfContent) {
                        content.musicShelfRenderer.contents.forEach { shelfContent ->
                            if (shelfContent is SectionContent.MusicResponsiveListItemContent) {
                                val songItem =
                                    shelfContent.musicResponsiveListItemRenderer.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs?.get(
                                        0
                                    )


                                if (songItem != null) {
                                    val navigationEndpoint = songItem?.navigationEndpoint
                                    if (navigationEndpoint != null && navigationEndpoint is NavigationEndpoint.WatchNavigationEndpoint) {
                                        val videoId = navigationEndpoint.watchEndpoint.videoId

                                        songs.add(
                                            Song(
                                                id = videoId,
                                                playlistId = playlistId,
                                                title = songItem.text,
                                                images = playlistImages
                                            )
                                        )
                                    }

                                }

                            }
                        }
                    }
                }

                PlaylistDetails(
                    name = playlistName,
                    artist = artist,
                    images = playlistImages,
                    songs = songs,
                    relatedPlaylists = relatedPlaylists
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }


    suspend fun artist(channelId: String): ArtistDetails? {
        return withContext(Dispatchers.IO) {
            try {

                val browseDataObject = browseDataFetcher.fetchBrowseData("/channel/$channelId")

                if (browseDataObject == null) {
                    Logger.getLogger("ArtistScraper")
                        .warning("Failed to fetch or parse browse data for channelId: $channelId")
                    return@withContext null
                }

                val topSongs = mutableListOf<Song>()
                val albums = mutableListOf<Playlist>()

                browseDataObject.contents.singleColumnBrowseResultsRenderer?.tabs?.forEach { tab ->
                    tab.tabRenderer.content.sectionListRenderer.contents.forEach { content ->
                        when (content) {
                            is SectionContent.MusicShelfContent -> {
                                if (content.musicShelfRenderer.title?.runs?.find {
                                        it.text == "Top songs"
                                    } != null) {

                                    content.musicShelfRenderer.contents.forEach { shelfItem ->
                                        if (shelfItem is SectionContent.MusicResponsiveListItemContent) {
                                            val titleFlex =
                                                shelfItem.musicResponsiveListItemRenderer.flexColumns.find { flex ->
                                                    flex.musicResponsiveListItemFlexColumnRenderer.text.runs?.find { run ->
                                                        run.navigationEndpoint != null && run.navigationEndpoint is NavigationEndpoint.WatchNavigationEndpoint
                                                                && run.navigationEndpoint.watchEndpoint.watchEndpointMusicSupportedConfigs?.watchEndpointMusicConfig?.musicVideoType == "MUSIC_VIDEO_TYPE_ATV"
                                                    } != null
                                                }

                                            val songImages =
                                                shelfItem.musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.map {
                                                    Image(
                                                        url = it.url,
                                                        width = it.width,
                                                        height = it.height
                                                    )
                                                }

                                            topSongs.add(
                                                Song(
                                                    id = shelfItem.musicResponsiveListItemRenderer.overlay.musicItemThumbnailOverlayRenderer.content.musicPlayButtonRenderer.playNavigationEndpoint.watchEndpoint.videoId,
                                                    playlistId = shelfItem.musicResponsiveListItemRenderer.overlay.musicItemThumbnailOverlayRenderer.content.musicPlayButtonRenderer.playNavigationEndpoint.watchEndpoint.playlistId,
                                                    images = songImages ?: emptyList(),
                                                    title = titleFlex?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.get(
                                                        0
                                                    )?.text ?: ""
                                                )
                                            )
                                        }

                                    }

                                }
                            }

                            is SectionContent.MusicCarouselContent -> {
                                if (content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.title.runs.find {
                                        it.text == "Albums"
                                    } != null) {

                                    content.musicCarouselShelfRenderer.contents.forEach { carouselItem ->

                                        albums.add(
                                            Playlist(
                                                id = (carouselItem.musicTwoRowItemRenderer.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId,
                                                name = carouselItem.musicTwoRowItemRenderer.title.runs[0].text,
                                                thumbnailUrl = carouselItem.musicTwoRowItemRenderer.thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.firstOrNull()?.url
                                                    ?: ""
                                            )
                                        )

                                    }

                                }
                            }

                            is SectionContent.MusicResponsiveListItemContent, is SectionContent.MusicResponsiveHeaderContent, is SectionContent.EmptyContent -> {
                                // DO NOTHING
                            }
                        }
                    }
                }

                val artistName = browseDataObject.microformat.microformatDataRenderer.title
                val description = browseDataObject.microformat.microformatDataRenderer.description
                val images =
                    browseDataObject.microformat.microformatDataRenderer.thumbnail.thumbnails.map {
                        Image(
                            url = it.url,
                            width = it.width,
                            height = it.height
                        )
                    }


                ArtistDetails(
                    name = artistName,
                    description = description,
                    images = images,
                    topSongs = topSongs,
                    albums = albums,
                    similarArtists = emptyList()
                )

            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

}