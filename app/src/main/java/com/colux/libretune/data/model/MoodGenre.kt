package com.colux.libretune.data.model

data class MoodGenreItem(
    val id: String,
    val name: String
)

data class MoodGenres(
    val moods: List<MoodGenreItem>,
    val genres: List<MoodGenreItem>
)