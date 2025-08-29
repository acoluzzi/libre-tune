package com.coluzziandrea.libretune_extractor.model

import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint.NavigationEndpoint

data class SongArtist(
    val id: String,
    val name: String
)

data class Song(
    val id: String,
    val artists: List<SongArtist>,
    val playlistId: String?,
    val title: String,
    val images: List<Image>,
) {
    companion object {
        fun from(shelfContent: SectionContent.MusicResponsiveListItemContent): Song? {
            val songItem =
                shelfContent.musicResponsiveListItemRenderer.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs?.get(
                    0
                )

            val artists =
                shelfContent.musicResponsiveListItemRenderer.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.filter { it.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint }
                    ?.map {
                        val artistId =
                            (it.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId
                        SongArtist(
                            id = artistId,
                            name = it.text
                        )
                    } ?: emptyList()

            val images =
                shelfContent.musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.map {
                    Image(
                        url = it.url,
                        width = it.width,
                        height = it.height
                    )
                } ?: emptyList()

            val playlistId =
                shelfContent.musicResponsiveListItemRenderer.overlay.musicItemThumbnailOverlayRenderer.content.musicPlayButtonRenderer.playNavigationEndpoint.watchEndpoint.playlistId

            if (songItem != null) {
                val navigationEndpoint = songItem.navigationEndpoint
                if (navigationEndpoint != null && navigationEndpoint is NavigationEndpoint.WatchNavigationEndpoint) {
                    val videoId = navigationEndpoint.watchEndpoint.videoId
                    return Song(
                        id = videoId,
                        playlistId = playlistId,
                        title = songItem.text,
                        artists = artists,
                        images = images
                    )

                }

            }
            return null
        }
    }
}