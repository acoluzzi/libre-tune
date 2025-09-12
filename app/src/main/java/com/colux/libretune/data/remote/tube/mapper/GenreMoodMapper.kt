package com.colux.libretune.data.remote.tube.mapper

import com.colux.libretune.data.model.GenreMoodCategoryPlaylistCarousel
import com.colux.libretune.data.model.MoodGenreCategory
import com.colux.libretune.data.model.MoodGenreItem
import com.colux.libretune.data.model.MoodGenres
import com.coluzziandrea.libretune_extractor.model.GenreMoodCategory
import com.coluzziandrea.libretune_extractor.model.GenresMoods

fun GenresMoods.toDataModel() = MoodGenres(
    moods = this.moods.map { MoodGenreItem(it.id, it.name) },
    genres = this.genres.map { MoodGenreItem(it.id, it.name) }
)


fun GenreMoodCategory.toDataModel() = MoodGenreCategory(
    name = this.name,
    songs = this.songs.map { it.toDataModel() },
    carousels = this.carousels.map { carousel ->
        GenreMoodCategoryPlaylistCarousel(
            title = carousel.title,
            playlists = carousel.playlists.map { it.toDataModel() }
        )
    }
)