package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.browse_response.BrowseData
import com.coluzziandrea.libretune_extractor.browse_response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.ArtistDetails
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.Song

class ArtistParser {

    companion object {
        fun from(browseDataObject: BrowseData): ArtistDetails? {
            val topSongs = mutableListOf<Song>()
            val albums = mutableListOf<Playlist>()
            val singlesEp = mutableListOf<Playlist>()
            val featuring = mutableListOf<Playlist>()
            val playlists = mutableListOf<Playlist>()
            val similarArtists = mutableListOf<Artist>()

            var topSongsPlaylist: Playlist? = null
            var discographyId: String? = null

            val artistName = browseDataObject.microformat?.microformatDataRenderer?.title
            val description = browseDataObject.microformat?.microformatDataRenderer?.description
            val images =
                browseDataObject.microformat?.microformatDataRenderer?.thumbnail?.thumbnails?.map {
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
                                        Song.from(shelfItem).let {
                                            if (it != null) {
                                                topSongs.add(it)
                                            }
                                        }
                                    }
                                }

                                if (content.musicShelfRenderer.bottomEndpoint != null && content.musicShelfRenderer.bottomEndpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
                                    topSongsPlaylist = Playlist(
                                        id = content.musicShelfRenderer.bottomEndpoint.browseEndpoint.browseId,
                                        name = "Top songs",
                                        images = emptyList()
                                    )
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
                                    content.musicCarouselShelfRenderer.contents.map(Playlist.Companion::from)
                                        .filter { it != null }
                                        .map { it!! }


                                when (headerText) {
                                    "Albums" -> {
                                        albums.addAll(currentPlaylists)
                                        discographyId =
                                            content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.moreContentButton?.buttonRenderer?.navigationEndpoint?.let { endpoint ->
                                                if (endpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
                                                    endpoint.browseEndpoint.browseId
                                                } else {
                                                    null
                                                }
                                            }
                                    }

                                    "Singles & EPs" -> {
                                        singlesEp.addAll(currentPlaylists)
                                        discographyId =
                                            content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.moreContentButton?.buttonRenderer?.navigationEndpoint?.let { endpoint ->
                                                if (endpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
                                                    endpoint.browseEndpoint.browseId
                                                } else {
                                                    null
                                                }
                                            }
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
                                    Artist.from(carouselItem).let {
                                        if (it != null) {
                                            similarArtists.add(it)
                                        }
                                    }
                                }
                            }


                        }

                        else -> {
                            // DO NOTHING
                        }
                    }
                }
            }


            if (artistName.isNullOrEmpty() || images.isNullOrEmpty()) {
                return null
            }



            return ArtistDetails(
                name = artistName,
                description = description,
                images = images,
                topSongs = topSongs,
                albums = albums,
                similarArtists = similarArtists,
                singlesAndEp = singlesEp,
                discographyId = discographyId,
                featuring = featuring,
                playlists = playlists,
                topSongsPlaylist = topSongsPlaylist
            )
        }

    }
}