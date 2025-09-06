package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.client.response.BrowseData
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.PlaylistDetails
import com.coluzziandrea.libretune_extractor.model.PlaylistType
import com.coluzziandrea.libretune_extractor.model.Song
import com.coluzziandrea.libretune_extractor.parser.mapper.toPlaylist
import com.coluzziandrea.libretune_extractor.parser.mapper.toSong

class PlaylistParser {

    companion object {
        fun from(browseDataObject: BrowseData, browseId: String): PlaylistDetails {
            var playlistName = ""
            val artists = mutableListOf<Artist>()
            val songs = mutableListOf<Song>()
            val relatedPlaylists = mutableListOf<Playlist>()
            val playlistImages = mutableListOf<Image>()
            var albumType = PlaylistType.PLAYLIST

            var releaseYear = 0

            val header =
                browseDataObject.contents.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()

            if (header is SectionContent.MusicResponsiveHeaderContent) {
                playlistName =
                    header.musicResponsiveHeaderRenderer.title.runs.firstOrNull()?.text ?: ""
                val artistName =
                    header.musicResponsiveHeaderRenderer.straplineTextOne?.runs?.firstOrNull()?.text
                val artistId =
                    header.musicResponsiveHeaderRenderer.straplineTextOne?.runs?.firstOrNull()?.navigationEndpoint?.let { endpoint ->
                        if (endpoint is com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint.BrowseNavigationEndpoint) {
                            endpoint.browseEndpoint.browseId
                        } else {
                            null
                        }
                    }

                releaseYear =
                    header.musicResponsiveHeaderRenderer.subtitle.runs.lastOrNull()?.text?.toIntOrNull()
                        ?: 0

                albumType =
                    header.musicResponsiveHeaderRenderer.subtitle.runs.firstOrNull()?.text?.let { subtitle ->
                        when {
                            subtitle.contains("Album", ignoreCase = true) -> PlaylistType.ALBUM
                            subtitle.contains(
                                "Single",
                                ignoreCase = true
                            ) || subtitle.contains(
                                "EP",
                                ignoreCase = true
                            ) -> PlaylistType.SINGLE_EP

                            else -> PlaylistType.PLAYLIST
                        }
                    } ?: PlaylistType.PLAYLIST

                if (!artistId.isNullOrEmpty() && !artistName.isNullOrEmpty()) {
                    artists.add(
                        Artist(
                            id = artistId,
                            name = artistName
                        )
                    )
                }

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
                if (content is SectionContent.MusicShelfContent || content is SectionContent.MusicPlaylistShelfContent) {
                    val contents = when (content) {
                        is SectionContent.MusicShelfContent -> content.musicShelfRenderer.contents
                        is SectionContent.MusicPlaylistShelfContent -> content.musicPlaylistShelfRenderer.contents
                        else -> null
                    }
                    contents?.forEach { shelfContent ->
                        if (shelfContent is SectionContent.MusicResponsiveListItemContent) {
                            shelfContent.musicResponsiveListItemRenderer.toSong().let {
                                if (it != null) {
                                    var images = it.images
                                    if (images.isEmpty()) {
                                        images = playlistImages
                                    }
                                    var songArtists = it.artists
                                    if (songArtists.isEmpty()) {
                                        songArtists = artists
                                    }
                                    var album = it.album
                                    if (album == null && albumType != PlaylistType.PLAYLIST) {
                                        album = Playlist(
                                            name = playlistName,
                                            id = browseId,
                                            type = albumType,
                                            images = playlistImages,
                                            artists = artists,
                                            releaseYear = -1
                                        )
                                    }
                                    songs.add(
                                        Song(
                                            id = it.id,
                                            artists = songArtists,
                                            playlistId = it.playlistId,
                                            title = it.title,
                                            album = album,
                                            images = images
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                if (content is SectionContent.MusicCarouselContent) {
                    val headerText =
                        content.musicCarouselShelfRenderer.header.musicCarouselShelfBasicHeaderRenderer.title.runs.firstOrNull()?.text

                    if (headerText == "Releases for you") {

                        val currentPlaylists =
                            content.musicCarouselShelfRenderer.contents.mapNotNull {
                                it.musicTwoRowItemRenderer?.toPlaylist()
                            }

                        relatedPlaylists.addAll(currentPlaylists)
                    }
                }
            }

            return PlaylistDetails(
                name = playlistName,
                artists = artists,
                images = playlistImages,
                songs = songs,
                relatedPlaylists = relatedPlaylists,
                type = albumType,
                releaseYear = releaseYear
            )
        }
    }
}