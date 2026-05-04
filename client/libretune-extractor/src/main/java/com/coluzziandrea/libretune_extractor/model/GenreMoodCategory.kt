package com.coluzziandrea.libretune_extractor.model

data class GenreMoodCategoryPlaylistCarousel(
    val title: String,
    val playlists: List<Playlist>
)

data class GenreMoodCategory(
    val name: String,
    val songs: List<Song>,
    val carousels: List<GenreMoodCategoryPlaylistCarousel>
)