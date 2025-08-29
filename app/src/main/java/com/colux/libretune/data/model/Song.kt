package com.colux.libretune.data.model

import com.coluzziandrea.libretune_extractor.model.Song as ExtractorSong

data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val imageUrl: String,
    val mediaUrl: String?,
) {
    companion object {

        fun from(song: ExtractorSong): Song {
            return Song(
                id = song.id,
                title = song.title,
                artist = song.artist,
                imageUrl = song.images.firstOrNull()?.url ?: "",
                mediaUrl = null,
            )
        }
    }

}