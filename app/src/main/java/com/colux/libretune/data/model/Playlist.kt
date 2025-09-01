package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Playlist(
    val id: String,
    val name: String,
    val images: List<Image>,
    val artists: List<Artist>
) : Parcelable {
    fun bestImageUrlForCarousel(): String? {
        return images.minByOrNull { it.width ?: 0 }?.url
    }
}
