package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.browse_response.BrowseData
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.ArtistDetails
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.Song

class ArtistParser {

    companion object {
        fun from(browseDataObject: BrowseData): ArtistDetails {
            val topSongs = mutableListOf<Song>()
            val albums = mutableListOf<Playlist>()
            val singlesEp = mutableListOf<Playlist>()
            val featuring = mutableListOf<Playlist>()
            val playlists = mutableListOf<Playlist>()
            val similarArtists = mutableListOf<Artist>()

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
                                                artist = artistName,
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

                            val headerText =
                                content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.title.runs.firstOrNull()?.text

                            if (listOf(
                                    "Albums",
                                    "Singles & EPs",
                                    "Featured on",
                                    "Playlists by $artistName"
                                ).contains(headerText)
                            ) {

                                val currentPlaylists =
                                    content.musicCarouselShelfRenderer.contents.map { carouselItem ->
                                        Playlist(
                                            id = (carouselItem.musicTwoRowItemRenderer.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId,
                                            name = carouselItem.musicTwoRowItemRenderer.title.runs[0].text,
                                            thumbnailUrl = carouselItem.musicTwoRowItemRenderer.thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.firstOrNull()?.url
                                                ?: ""
                                        )
                                    }


                                when (headerText) {
                                    "Albums" -> {
                                        albums.addAll(currentPlaylists)
                                    }

                                    "Singles & EPs" -> {
                                        singlesEp.addAll(currentPlaylists)
                                    }

                                    "Featured on" -> {
                                        featuring.addAll(currentPlaylists)
                                    }

                                    "Playlists by $artistName" -> {
                                        playlists.addAll(currentPlaylists)
                                    }
                                }

                            }

                            if (headerText == "Fans might also like") {
                                content.musicCarouselShelfRenderer.contents.forEach { carouselItem ->
                                    similarArtists.add(
                                        Artist(
                                            id = (carouselItem.musicTwoRowItemRenderer.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId,
                                            name = carouselItem.musicTwoRowItemRenderer.title.runs[0].text,
                                            images = carouselItem.musicTwoRowItemRenderer.thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.map {
                                                Image(
                                                    url = it.url,
                                                    width = it.width,
                                                    height = it.height
                                                )
                                            }
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




            return ArtistDetails(
                name = artistName,
                description = description,
                images = images,
                topSongs = topSongs,
                albums = albums,
                similarArtists = similarArtists,
                singlesAndEp = singlesEp,
                featuring = featuring,
                playlists = playlists
            )
        }

    }
}