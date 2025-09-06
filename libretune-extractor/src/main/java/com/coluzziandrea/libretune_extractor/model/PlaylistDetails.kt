package com.coluzziandrea.libretune_extractor.model

data class PlaylistDetails(
    val name: String,
    val releaseYear: Int,
    val type: PlaylistType,
    val images: List<Image>,
    val artists: List<Artist>,
    val songs: List<Song>,
    val relatedPlaylists: List<Playlist>
)