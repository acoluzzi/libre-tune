package com.colux.libretune.data.model


data class GenreMoodCategoryPlaylistCarousel(
    val title: String,
    val playlists: List<Playlist>
)

data class MoodGenreCategory(
    val name: String,
    val songs: List<Song>,
    val carousels: List<GenreMoodCategoryPlaylistCarousel>
)