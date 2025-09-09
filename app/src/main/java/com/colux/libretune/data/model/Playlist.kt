package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class PlaylistType { ALBUM, SINGLE, EP, PLAYLIST }

@Parcelize
data class Playlist(
    val id: String,
    val name: String,
    val images: List<Image>,
    val artists: List<Artist>,
    val type: PlaylistType = PlaylistType.PLAYLIST,
    val releaseYear: Int? = null,
    val isLocal: Boolean
) : Parcelable {
    fun bestImageUrlForCarousel(): String? {
        return images.maxByOrNull { it.width ?: 0 }?.url
    }

    fun getArtistNames(): String {
        return artists.joinToString(", ") { it.name }
    }

    fun getSubtitle(): String {
        val typeLabel = when (type) {
            PlaylistType.ALBUM -> "Album"
            PlaylistType.SINGLE -> "Single"
            PlaylistType.EP -> "EP"
            PlaylistType.PLAYLIST -> "Playlist"
        }
        return if (type == PlaylistType.PLAYLIST) {
            typeLabel
        } else {
            val artistNamesStr = if (this.artists.isNotEmpty()) {
                "• ${getArtistNames()}"
            } else {
                ""
            }
            val releaseYearStr = releaseYear?.let {
                "• $it"
            } ?: ""
            "$typeLabel $artistNamesStr $releaseYearStr"
        }
    }
}
