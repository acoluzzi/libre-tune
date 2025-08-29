package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val images: List<Image>
) : Parcelable {
    companion object {

        fun from(song: com.coluzziandrea.libretune_extractor.model.Song): Song {
            return Song(
                id = song.id,
                title = song.title,
                artists = song.artists.map {
                    Artist(
                        id = it.id,
                        name = it.name
                    )
                },
                images = song.images.map {
                    Image(
                        url = it.url,
                        width = it.width,
                        height = it.height
                    )
                },
            )
        }
    }


    fun getArtistNames(): String {
        return artists.joinToString(", ") { it.name }
    }

    fun getBestImageUrl(): String? {
        return images.maxByOrNull { it.width ?: 0 }?.url
    }

}