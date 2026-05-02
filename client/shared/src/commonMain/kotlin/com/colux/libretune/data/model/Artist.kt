package com.colux.libretune.data.model

import com.colux.libretune.shared.parcelable.Parcelable
import com.colux.libretune.shared.parcelable.Parcelize

@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val images: List<Image>
) : Parcelable {
    fun bestImageForCarousel(): String? {
        return images.minByOrNull { it.width ?: 0 }?.url
    }
}

