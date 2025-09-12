package com.coluzziandrea.libretune_extractor.model

data class MoodGenreItem(
    val id: String,
    val name: String,
)


data class GenresMoods(
    val moods: List<MoodGenreItem>,
    val genres: List<MoodGenreItem>,
)