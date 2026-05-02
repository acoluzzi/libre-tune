package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.client.response.section.content.MusicResponsiveListItemRenderer
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.MusicNode

fun MusicResponsiveListItemRenderer.extractArtistsInfo(): List<MusicNode> {
    val artists = mutableListOf<MusicNode>()

    val artistFlexColumn = flexColumns.find {
        it.musicResponsiveListItemFlexColumnRenderer.text.runs?.any { run ->
            run.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && run.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_ARTIST"
        } == true
    }

    artistFlexColumn?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.filter { it.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && it.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_ARTIST" }
        ?.forEach {
            val artistId =
                (it.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId
            val result = MusicNode(
                id = artistId,
                name = it.text
            )
            artists.add(result)
        }
    return artists

}


fun MusicResponsiveListItemRenderer.toArtist(): Artist? {
    val name =
        flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.get(
            0
        )?.text
    val navigationEndpoint = navigationEndpoint
    if (navigationEndpoint !is NavigationEndpoint.BrowseNavigationEndpoint) {
        return null
    }
    val id = navigationEndpoint.browseEndpoint.browseId
    if (name.isNullOrEmpty() || id.isEmpty()) {
        return null
    }
    return Artist(
        id = id,
        name = name,
        images = thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.map {
            Image(
                url = it.url,
                width = it.width,
                height = it.height
            )
        } ?: emptyList()
    )
}