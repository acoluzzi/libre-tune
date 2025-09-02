package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class PlaylistType { ALBUM, SINGLE_EP, PLAYLIST }

@Parcelize
data class Playlist(
    val id: String,
    val name: String,
    val images: List<Image>,
    val artists: List<Artist>,
    val type: PlaylistType = PlaylistType.PLAYLIST
) : Parcelable {
    fun bestImageUrlForCarousel(): String? {
        return images.minByOrNull { it.width ?: 0 }?.url
    }

    fun getArtistNames(): String {
        return artists.joinToString(", ") { it.name }
    }
}
