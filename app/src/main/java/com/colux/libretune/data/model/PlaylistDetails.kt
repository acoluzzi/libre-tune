package com.colux.libretune.data.model

data class PlaylistDetails(
    val name: String,
    val bannerUrl: String?,
    val artist: String,
    val songs: List<Song>,
    val relatedPlaylists: List<Playlist>
)