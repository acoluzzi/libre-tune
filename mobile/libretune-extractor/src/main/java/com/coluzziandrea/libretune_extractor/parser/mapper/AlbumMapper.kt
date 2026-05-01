package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.client.response.section.content.MusicTwoRowsItemRenderer
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.MusicNode
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.PlaylistType

fun SectionContent.MusicResponsiveListItemContent.extractAlbumInfo(): MusicNode? {

    val albumFlexColumn = musicResponsiveListItemRenderer.flexColumns.find {
        it.musicResponsiveListItemFlexColumnRenderer.text.runs?.any { run ->
            run.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && run.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_ALBUM"
        } == true
    }

    albumFlexColumn?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.filter { it.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && it.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_ALBUM" }
        ?.first {
            val id =
                (it.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId
            val result = MusicNode(
                id = id,
                name = it.text
            )
            return result
        }
    return null

}


fun MusicTwoRowsItemRenderer.toPlaylist(): Playlist? {

    val id =
        (navigationEndpoint as? NavigationEndpoint.BrowseNavigationEndpoint)?.browseEndpoint?.browseId


    val name =
        title.runs.firstOrNull()?.text?.trim()

    if (id == null || name == null || id.isEmpty() || name.isEmpty()) {
        return null
    }

    val images = thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.map {
        Image(
            url = it.url,
            width = it.width,
            height = it.height
        )
    }

    val playlistTypeStr = subtitle.runs.firstOrNull()?.text ?: ""
    val releaseYearStr = subtitle.runs.getOrNull(2)?.text ?: subtitle.runs.getOrNull(0)?.text ?: ""

    val artists = mutableListOf<Artist>()
    subtitle.runs.filter { it.navigationEndpoint is NavigationEndpoint.BrowseNavigationEndpoint && it.navigationEndpoint.browseEndpoint.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == "MUSIC_PAGE_TYPE_ARTIST" }
        .forEach {
            val artistId =
                (it.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId
            val artistName = it.text
            if (artistId.isNotEmpty() && artistName.isNotEmpty()) {
                artists.add(
                    Artist(
                        id = artistId,
                        name = artistName
                    )
                )
            }
        }


    val playlistType = when {
        playlistTypeStr.contains("Album", ignoreCase = true) -> PlaylistType.ALBUM
        playlistTypeStr.contains("Single", ignoreCase = true) -> PlaylistType.SINGLE
        playlistTypeStr.contains("EP", ignoreCase = true) -> PlaylistType.EP

        else -> PlaylistType.PLAYLIST
    }

    return Playlist(
        id = id,
        name = name,
        images = images,
        artists = artists,
        type = playlistType,
        releaseYear = releaseYearStr.toIntOrNull() ?: -1,
    )
}