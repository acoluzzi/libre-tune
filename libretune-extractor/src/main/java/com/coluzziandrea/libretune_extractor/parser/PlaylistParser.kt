package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.client.response.BrowseData
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.PlaylistDetails
import com.coluzziandrea.libretune_extractor.model.Song
import com.coluzziandrea.libretune_extractor.parser.mapper.toSong

class PlaylistParser {

    companion object {
        fun from(browseDataObject: BrowseData): PlaylistDetails {
            var playlistName = ""
            var artist: String? = null
            val songs = mutableListOf<Song>()
            val relatedPlaylists = mutableListOf<Playlist>()
            val playlistImages = mutableListOf<Image>()


            val header =
                browseDataObject.contents.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()

            if (header is SectionContent.MusicResponsiveHeaderContent) {
                playlistName =
                    header.musicResponsiveHeaderRenderer.title.runs.firstOrNull()?.text ?: ""
                artist =
                    header.musicResponsiveHeaderRenderer.straplineTextOne?.runs?.firstOrNull()?.text


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
                                    songs.add(
                                        Song(
                                            id = it.id,
                                            artists = it.artists,
                                            playlistId = it.playlistId,
                                            title = it.title,
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
                            content.musicCarouselShelfRenderer.contents.map(Playlist.Companion::from)
                                .filter { it != null }
                                .map { it!! }

                        relatedPlaylists.addAll(currentPlaylists)
                    }
                }
            }

            return PlaylistDetails(
                name = playlistName,
                artist = artist,
                images = playlistImages,
                songs = songs,
                relatedPlaylists = relatedPlaylists
            )
        }
    }
}