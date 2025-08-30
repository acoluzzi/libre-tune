package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.coluzziandrea.libretune_extractor.model.Artist as ExtractorArtist

@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val images: List<Image>
) : Parcelable {
    companion object {
        fun from(raw: ExtractorArtist): Artist {
            return Artist(
                id = raw.id,
                name = raw.name,
                images = raw.images.map { image ->
                    Image(
                        url = image.url,
                        width = image.width,
                        height = image.height
                    )
                }
            )
        }
    }

    fun bestImageForCarousel() {
        images.minByOrNull { it.width ?: 0 }?.url
    }
}

