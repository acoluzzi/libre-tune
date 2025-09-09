package com.colux.libretune.data.model.wrapper

import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.Song

data class PlaylistWithSongs(
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList()
) {
    fun getImages(): List<String> {
        return songs
            .mapNotNull { it.images.firstOrNull()?.url }
            .distinctBy {
                it
            }
            .shuffled().take(4)
    }

    fun hasToShowCollage(): Boolean {
        return playlist?.isLocal == true && songs.isNotEmpty()
    }

    fun bestImage(): String? {
        return playlist?.images?.maxByOrNull { it.width ?: 0 }?.url
    }

    fun getSubtitle(): String {
        var subtitle = playlist?.getSubtitle() ?: ""
        if (songs.isNotEmpty()) {
            subtitle += " • ${songs.size} song${if (songs.size > 1) "s" else ""}"
        }
        return subtitle
    }
}