package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.GenericMusicItem


fun SectionContent.MusicResponsiveListItemContent.toMusicItem(): GenericMusicItem? {
    return when (musicResponsiveListItemRenderer.navigationEndpoint) {
        is NavigationEndpoint.WatchNavigationEndpoint -> {
            return GenericMusicItem.SongResult(
                song = musicResponsiveListItemRenderer.toSong()
            )
        }

        is NavigationEndpoint.BrowseNavigationEndpoint -> {
            val pageType =
                musicResponsiveListItemRenderer.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType

            when (pageType) {
                "MUSIC_PAGE_TYPE_ARTIST" -> {
                    return GenericMusicItem.ArtistResult(
                        artist = musicResponsiveListItemRenderer.toArtist()
                    )
                }

                "MUSIC_PAGE_TYPE_ALBUM" -> {
                    return GenericMusicItem.AlbumResult(
                        album = musicResponsiveListItemRenderer.toPlaylist()
                    )
                }

                "MUSIC_PAGE_TYPE_PLAYLIST" -> {
                    return GenericMusicItem.PlaylistResult(
                        playlist = musicResponsiveListItemRenderer.toPlaylist()
                    )
                }

                else -> null
            }
        }

        else -> null
    }
}