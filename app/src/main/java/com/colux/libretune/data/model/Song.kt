package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.coluzziandrea.libretune_extractor.model.Song as ExtractorSong

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artist: String?,
    val imageUrl: String,
    val mediaUrl: String? = null
) : Parcelable {
    companion object {

        fun from(song: ExtractorSong): Song {
            return Song(
                id = song.id,
                title = song.title,
                artist = song.artists.map {
                    it.name
                }.joinToString(", ") {
                    it
                },
                imageUrl = song.images.firstOrNull()?.url ?: "",
            )
        }
    }

}