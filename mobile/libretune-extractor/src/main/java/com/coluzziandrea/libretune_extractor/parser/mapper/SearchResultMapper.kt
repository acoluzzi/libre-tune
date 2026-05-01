package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.GenericMusicItem

fun SectionContent.MusicCardShelfContent.toTopResult(): GenericMusicItem? {
    val navigationEndpoint =
        musicCardShelfRenderer.title.runs.firstOrNull()?.navigationEndpoint

    if (navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
        val pageType =
            navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType

        when (pageType) {
            "MUSIC_PAGE_TYPE_ARTIST" -> {
                val artistGenericMusicItem = GenericMusicItem.ArtistResult(
                    artist = Artist.from(musicCardShelfRenderer)
                )
                return artistGenericMusicItem
            }

            "MUSIC_PAGE_TYPE_ALBUM" -> {
                val albumGenericMusicItem = GenericMusicItem.AlbumResult(
                    album = musicCardShelfRenderer.toPlaylist()
                )
                return albumGenericMusicItem
            }
        }
    }
    return null
}