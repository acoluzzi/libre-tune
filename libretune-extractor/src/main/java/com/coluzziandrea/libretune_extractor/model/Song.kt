package com.coluzziandrea.libretune_extractor.model

import com.coluzziandrea.libretune_extractor.browse_response.section.content.FlexColumn
import com.coluzziandrea.libretune_extractor.browse_response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.parser.mapper.extractArtistsInfo


data class Song(
    val node: MusicNode,
    val artists: List<Artist>,
    val album: Playlist? = null,
    val playlistId: String?,
    val images: List<Image>,
) {
    val title: String
        get() = node.name

    val id: String
        get() = node.id

    constructor(
        id: String,
        title: String,
        artists: List<Artist>,
        album: Playlist? = null,
        playlistId: String?,
        images: List<Image>,
    ) : this(
        node = MusicNode(
            id = id,
            name = title
        ),
        artists = artists,
        album = album,
        playlistId = playlistId,
        images = images
    )

    companion object {


        fun getAlbum(flexColumns: List<FlexColumn>): Playlist? {
            val albumFlexColumn = flexColumns.find {
                it.musicResponsiveListItemFlexColumnRenderer.text.runs?.any { run ->
                    run.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && run.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs.browseEndpointContextMusicConfig.pageType == "MUSIC_PAGE_TYPE_ALBUM"
                } == true
            }

            albumFlexColumn?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.filter { it.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && it.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs.browseEndpointContextMusicConfig.pageType == "MUSIC_PAGE_TYPE_ALBUM" }
                ?.forEach {
                    val albumId =
                        (it.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId

                    return Playlist(
                        id = albumId,
                        name = it.text,
                        images = emptyList()
                    )
                }
            return null
        }


        fun from(shelfContent: SectionContent.MusicResponsiveListItemContent): Song? {
            val songItem =
                shelfContent.musicResponsiveListItemRenderer.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs?.get(
                    0
                )

            val artists = shelfContent.extractArtistsInfo().map {
                Artist(
                    id = it.id,
                    name = it.name
                )
            }

            val album = getAlbum(shelfContent.musicResponsiveListItemRenderer.flexColumns)

            val images =
                shelfContent.musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.map {
                    Image(
                        url = it.url,
                        width = it.width,
                        height = it.height
                    )
                } ?: emptyList()

            val playlistId =
                shelfContent.musicResponsiveListItemRenderer.overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.let {
                    if (it is NavigationEndpoint.WatchNavigationEndpoint) {
                        it.watchEndpoint.playlistId
                    } else {
                        null
                    }
                }

            if (songItem != null) {
                val navigationEndpoint = songItem.navigationEndpoint
                if (navigationEndpoint != null && navigationEndpoint is NavigationEndpoint.WatchNavigationEndpoint) {
                    val videoId = navigationEndpoint.watchEndpoint.videoId
                    if (videoId.isNullOrEmpty()) {
                        return null
                    }
                    return Song(
                        id = videoId,
                        playlistId = playlistId,
                        album = album,
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