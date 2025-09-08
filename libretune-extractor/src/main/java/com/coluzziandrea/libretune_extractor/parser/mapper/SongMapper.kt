package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.client.response.section.content.FlexColumn
import com.coluzziandrea.libretune_extractor.client.response.section.content.MusicResponsiveListItemRenderer
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.Artist
import com.coluzziandrea.libretune_extractor.model.Image
import com.coluzziandrea.libretune_extractor.model.Playlist
import com.coluzziandrea.libretune_extractor.model.Song
import com.coluzziandrea.libretune_extractor.parser.util.toSuffixedLong

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
                images = emptyList(),
                releaseYear = -1
            )
        }
    return null
}

fun getViews(flexColumns: List<FlexColumn>): Long {
    val viewsFlexColumn = flexColumns.find {
        it.musicResponsiveListItemFlexColumnRenderer.text.runs?.any { run ->
            run.navigationEndpoint == null && run.text.contains("plays", ignoreCase = true)
        } == true
    }

    viewsFlexColumn?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.filter {
        it.navigationEndpoint == null && it.text.contains(
            "plays",
            ignoreCase = true
        )
    }
        ?.forEach {
            return it.text.replace("plays", "", ignoreCase = true).replace(",", "").trim()
                .toSuffixedLong()
                ?: 0L
        }
    return 0L
}


fun MusicResponsiveListItemRenderer.toSong(): Song? {
    val songItem =
        flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs?.get(
            0
        )

    val artists = this.extractArtistsInfo().map {
        Artist(
            id = it.id,
            name = it.name
        )
    }

    val album = getAlbum(flexColumns)

    val images =
        thumbnail?.musicThumbnailRenderer?.thumbnail?.thumbnails?.map {
            Image(
                url = it.url,
                width = it.width,
                height = it.height
            )
        } ?: emptyList()

    val playlistId =
        overlay?.musicItemThumbnailOverlayRenderer?.content?.musicPlayButtonRenderer?.playNavigationEndpoint?.let {
            if (it is NavigationEndpoint.WatchNavigationEndpoint) {
                it.watchEndpoint.playlistId
            } else {
                null
            }
        }

    val durationStr =
        fixedColumns?.firstOrNull()?.musicResponsiveListItemFixedColumnRenderer?.text?.runs?.firstOrNull()?.text

    val durationSec = durationStr?.let {
        val parts = it.split(":").map { part -> part.toLongOrNull() ?: 0L }
        when (parts.size) {
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> 0L
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
                views = getViews(flexColumns),
                playlistId = playlistId,
                album = album?.copy(
                    artists = artists
                ),
                title = songItem.text,
                artists = artists,
                images = images,
                durationSec = durationSec
            )

        }

    }
    return null
}