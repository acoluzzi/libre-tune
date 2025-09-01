package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val images: List<Image>
) : Parcelable {
    fun bestImageForCarousel() {
        images.minByOrNull { it.width ?: 0 }?.url
    }
}

