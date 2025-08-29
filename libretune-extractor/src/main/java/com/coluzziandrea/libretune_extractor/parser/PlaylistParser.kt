package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.browse_response.BrowseData
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.PlaylistDetails
import com.coluzziandrea.libretune_extractor.model.Song

class PlaylistParser {

    companion object {
        fun from(browseDataObject: BrowseData): PlaylistDetails {
            var playlistName = ""
            var artist = ""
            val songs = mutableListOf<Song>()
            val relatedPlaylists = mutableListOf<Playlist>()
            val playlistImages = mutableListOf<Image>()

            val playlistId = browseDataObject.microformat.microformatDataRenderer.urlCanonical
                .replace("https://music.youtube.com/playlist?list=", "")

            val header =
                browseDataObject.contents.twoColumnBrowseResultsRenderer?.tabs?.firstOrNull()?.tabRenderer?.content?.sectionListRenderer?.contents?.firstOrNull()

            if (header is SectionContent.MusicResponsiveHeaderContent) {
                playlistName =
                    header.musicResponsiveHeaderRenderer.title.runs.firstOrNull()?.text ?: ""
                artist =
                    header.musicResponsiveHeaderRenderer.straplineTextOne?.runs?.firstOrNull()?.text
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
                                            artist = artist,
                                            images = playlistImages
                                        )
                                    )
                                }

                            }

                        }
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