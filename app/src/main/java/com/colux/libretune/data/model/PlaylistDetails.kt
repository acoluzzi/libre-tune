package com.colux.libretune.data.model

data class PlaylistDetails(
    val id: String,
    val name: String,
    val isLocal: Boolean,
    val type: PlaylistType,
    val images: List<Image>,
    val artists: List<Artist>,
    val songs: List<Song>,
    val relatedPlaylists: List<Playlist>
) {
    fun bestImage(): String? {
        return images.maxByOrNull { it.width ?: 0 }?.url
    }

    fun getArtistNames(): String {
        return artists.joinToString(", ") { it.name }
    }
}