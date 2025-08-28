package com.coluzziandrea.libretune_extractor.model

data class PlaylistDetails(
    val name: String,
    val images: List<Image>,
    val artist: String,
    val songs: List<Song>,
    val relatedPlaylists: List<Playlist>
)