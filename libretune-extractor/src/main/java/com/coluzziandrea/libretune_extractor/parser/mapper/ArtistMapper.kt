package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.browse_response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.MusicNode

fun SectionContent.MusicResponsiveListItemContent.extractArtistsInfo(): List<MusicNode> {
    val artists = mutableListOf<MusicNode>()

    val artistFlexColumn = musicResponsiveListItemRenderer.flexColumns.find {
        it.musicResponsiveListItemFlexColumnRenderer.text.runs?.any { run ->
            run.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && run.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs.browseEndpointContextMusicConfig.pageType == "MUSIC_PAGE_TYPE_ARTIST"
        } == true
    }

    artistFlexColumn?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.filter { it.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && it.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs.browseEndpointContextMusicConfig.pageType == "MUSIC_PAGE_TYPE_ARTIST" }
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

