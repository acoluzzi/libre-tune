package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.browse_response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.TopResult

fun SectionContent.MusicCardShelfContent.toTopResult(): TopResult? {
    val navigationEndpoint =
        musicCardShelfRenderer.title.runs.firstOrNull()?.navigationEndpoint

    if (navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint) {
        val pageType =
            navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType

        when (pageType) {
            "MUSIC_PAGE_TYPE_ARTIST" -> {
                val artistTopResult = TopResult.ArtistResult(
                    artist = Artist.from(musicCardShelfRenderer)
                )
                return artistTopResult
            }

            "MUSIC_PAGE_TYPE_ALBUM" -> {
                val albumTopResult = TopResult.AlbumResult(
                    album = musicCardShelfRenderer.toPlaylist()
                )
                return albumTopResult
            }
        }
    }
    return null
}