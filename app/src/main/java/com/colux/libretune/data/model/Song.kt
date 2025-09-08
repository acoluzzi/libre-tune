package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val album: Playlist? = null,
    val views: Long,
    val trackNumber: Int? = null,
    val images: List<Image>,
    val durationSec: Long? = null
) : Parcelable {

    fun getArtistNames(): String {
        return artists.joinToString(", ") { it.name }
    }

    fun getBestImageUrl(): String? {
        return images.maxByOrNull { it.width ?: 0 }?.url
    }

}