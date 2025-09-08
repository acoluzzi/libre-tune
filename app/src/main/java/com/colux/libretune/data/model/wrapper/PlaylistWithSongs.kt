package com.colux.libretune.data.model.wrapper

import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.Song

data class PlaylistWithSongs(
    val playlist: Playlist? = null,
    val songs: List<Song> = emptyList()
) {
    fun getImages(): List<String> {
        return songs.shuffled().take(4).mapNotNull { it.images.firstOrNull()?.url }
    }

    fun hasToShowCollage(): Boolean {
        return playlist?.isLocal == true && songs.isNotEmpty()
    }

    fun bestImage(): String? {
        return playlist?.images?.maxByOrNull { it.width ?: 0 }?.url
    }
}