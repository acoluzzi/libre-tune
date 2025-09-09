package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.client.response.BrowseData
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.ArtistDetails
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.PlaylistType
import com.coluzziandrea.libretune_extractor.model.Song
import com.coluzziandrea.libretune_extractor.parser.mapper.toPlaylist
import com.coluzziandrea.libretune_extractor.parser.mapper.toSong

class ArtistParser {

    companion object {
        fun from(browseDataObject: BrowseData, browseId: String): ArtistDetails? {
            val topSongs = mutableListOf<Song>()
            val albums = mutableListOf<Playlist>()
            val singlesEp = mutableListOf<Playlist>()
            val featuring = mutableListOf<Playlist>()
            val playlists = mutableListOf<Playlist>()
            val similarArtists = mutableListOf<Artist>()

            var albumDiscographyId: String? = null
            var singlesDiscographyId: String? = null

            var discographyAlbumsParam: String? = null
            var discographySinglesParam: String? = null

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

                                content.musicShelfRenderer.contents.forEachIndexed { index, shelfItem ->
                                    if (shelfItem is SectionContent.MusicResponsiveListItemContent) {
                                        shelfItem.musicResponsiveListItemRenderer.toSong(index)
                                            .let { song ->
                                                if (song != null) {
                                                    topSongs.add(song.copy(trackNumber = null))
                                                }
                                            }
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
                                    content.musicCarouselShelfRenderer.contents.mapNotNull {
                                        it.musicTwoRowItemRenderer?.toPlaylist()
                                    }


                                when (headerText) {
                                    "Albums" -> {
                                        albums.addAll(currentPlaylists.map {
                                            if (it.type != PlaylistType.ALBUM) {
                                                it.copy(
                                                    type = PlaylistType.ALBUM, artists =
                                                        listOf(
                                                            Artist(
                                                                id = browseId,
                                                                name = artistName ?: "Unknown"
                                                            )
                                                        )
                                                )
                                            } else {
                                                it.copy(
                                                    artists =
                                                        listOf(
                                                            Artist(
                                                                id = browseId,
                                                                name = artistName ?: "Unknown"
                                                            )
                                                        )
                                                )
                                            }
                                        })
                                        albumDiscographyId =
                                            content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.moreContentButton?.buttonRenderer?.navigationEndpoint?.let { endpoint ->
                                                if (endpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
                                                    endpoint.browseEndpoint.browseId
                                                } else {
                                                    null
                                                }
                                            }
                                        discographyAlbumsParam =
                                            content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.moreContentButton?.buttonRenderer?.navigationEndpoint?.let { endpoint ->
                                                if (endpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
                                                    endpoint.browseEndpoint.params
                                                } else {
                                                    null
                                                }
                                            }
                                    }

                                    "Singles & EPs" -> {
                                        singlesEp.addAll(currentPlaylists.map {
                                            if (it.type == PlaylistType.SINGLE || it.type == PlaylistType.EP) {
                                                it.copy(
                                                    artists =
                                                        listOf(
                                                            Artist(
                                                                id = browseId,
                                                                name = artistName ?: "Unknown"
                                                            )
                                                        )
                                                )
                                            } else {
                                                it.copy(
                                                    type = PlaylistType.SINGLE, artists =
                                                        listOf(
                                                            Artist(
                                                                id = browseId,
                                                                name = artistName ?: "Unknown"
                                                            )
                                                        )
                                                )
                                            }
                                        })
                                        singlesDiscographyId =
                                            content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.moreContentButton?.buttonRenderer?.navigationEndpoint?.let { endpoint ->
                                                if (endpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
                                                    endpoint.browseEndpoint.browseId
                                                } else {
                                                    null
                                                }
                                            }
                                        discographySinglesParam =
                                            content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.moreContentButton?.buttonRenderer?.navigationEndpoint?.let { endpoint ->
                                                if (endpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
                                                    endpoint.browseEndpoint.params
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
                discographyId = albumDiscographyId ?: singlesDiscographyId,
                featuring = featuring,
                playlists = playlists,
                discographyAlbumsParam = discographyAlbumsParam,
                discographySinglesParam = discographySinglesParam
            )
        }

    }
}