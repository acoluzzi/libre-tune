package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.client.response.section.content.MusicCardShelfRenderer
import com.coluzziandrea.libretune_extractor.client.response.section.content.MusicResponsiveListItemRenderer
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
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

    val typeStr = subtitle?.runs?.firstOrNull()?.text

    val playlistType = when {
        typeStr?.contains(
            "Album",
            ignoreCase = true
        ) == true -> com.coluzziandrea.libretune_extractor.model.PlaylistType.ALBUM

        typeStr?.contains("Single", ignoreCase = true) == true || typeStr?.contains(
            "EP",
            ignoreCase = true
        ) == true -> com.coluzziandrea.libretune_extractor.model.PlaylistType.SINGLE_EP

        else -> com.coluzziandrea.libretune_extractor.model.PlaylistType.PLAYLIST
    }

    return Playlist(
        id = id,
        name = name,
        type = playlistType,
        releaseYear = -1,
        images = images,
        artists = artists
    )
}


fun SectionContent.toPlaylist(): Playlist? {
    return when (this) {
        is SectionContent.MusicResponsiveListItemContent -> {
            this.musicResponsiveListItemRenderer.toPlaylist()
        }

        else -> null
    }
}

fun MusicResponsiveListItemRenderer.toPlaylist(): Playlist? {


    val id =
        (navigationEndpoint as? NavigationEndpoint.BrowseNavigationEndpoint)?.browseEndpoint?.browseId


    val name =
        flexColumns.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text

    if (id == null || name == null || id.isEmpty() || name.isEmpty()) {
        return null
    }

    val images =
        thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.map {
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

    val typeStr =
        flexColumns.lastOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text

    val releaseYearStr =
        flexColumns.lastOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.lastOrNull()?.text

    val playlistType = when {
        typeStr?.contains(
            "Album",
            ignoreCase = true
        ) == true -> com.coluzziandrea.libretune_extractor.model.PlaylistType.ALBUM

        typeStr?.contains("Single", ignoreCase = true) == true || typeStr?.contains(
            "EP",
            ignoreCase = true
        ) == true -> com.coluzziandrea.libretune_extractor.model.PlaylistType.SINGLE_EP

        else -> com.coluzziandrea.libretune_extractor.model.PlaylistType.PLAYLIST
    }

    return Playlist(
        id = id,
        name = name,
        images = images,
        artists = artists,
        type = playlistType,
        releaseYear = if (releaseYearStr != null && releaseYearStr.all { it.isDigit() }) {
            releaseYearStr.toInt()
        } else {
            -1
        },
    )
}