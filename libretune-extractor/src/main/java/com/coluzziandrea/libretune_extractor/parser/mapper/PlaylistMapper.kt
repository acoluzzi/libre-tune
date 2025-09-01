package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.browse_response.section.content.MusicCardShelfRenderer
import com.coluzziandrea.libretune_extractor.browse_response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist

fun MusicCardShelfRenderer.toPlaylist(): Playlist? {

    val navigationEndpoint =
        title.runs.firstOrNull()?.navigationEndpoint

    if (navigationEndpoint !is NavigationEndpoint.BrowseNavigationEndpoint) {
        return null
    }

    val artists = subtitle?.runs?.filter { run ->
        run.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint
    }?.map { subtitleRun ->
        val navEndpoint =
            subtitleRun.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint
        Artist(
            id = navEndpoint.browseEndpoint.browseId,
            name = subtitleRun.text
        )
    }

    val id = navigationEndpoint.browseEndpoint.browseId

    val name = title.runs.firstOrNull()?.text

    if (name == null || id.isEmpty() || name.isEmpty()) {
        return null
    }

    val images =
        thumbnail.musicThumbnailRenderer.thumbnail.thumbnails.map {
            Image(
                url = it.url,
                width = it.width,
                height = it.height
            )
        }


    return Playlist(
        id = id,
        name = name,
        images = images,
        artists = artists
    )
}


fun SectionContent.toPlaylist(): Playlist? {
    return when (this) {
        is SectionContent.MusicResponsiveListItemContent -> {
            this.toPlaylist()
        }

        else -> null
    }
}

fun SectionContent.MusicResponsiveListItemContent.toPlaylist(): Playlist? {


    val id =
        (musicResponsiveListItemRenderer.navigationEndpoint as? NavigationEndpoint.BrowseNavigationEndpoint)?.browseEndpoint?.browseId


    val name =
        musicResponsiveListItemRenderer.flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text

    if (id == null || name == null || id.isEmpty() || name.isEmpty()) {
        return null
    }

    val images =
        musicResponsiveListItemRenderer.thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.map {
            Image(
                url = it.url,
                width = it.width,
                height = it.height
            )
        } ?: emptyList()

    val artists = extractArtistsInfo().map {
        Artist(
            id = it.id,
            name = it.name
        )
    }

    return Playlist(
        id = id,
        name = name,
        images = images,
        artists = artists
    )
}