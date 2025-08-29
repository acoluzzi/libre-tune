package com.colux.libretune.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.coluzziandrea.libretune_extractor.model.Artist as ExtractorArtist

@Parcelize
data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String? = null
) : Parcelable {
    companion object {
        fun from(raw: ExtractorArtist): Artist {
            return Artist(
                id = raw.id,
                name = raw.name,
                imageUrl = raw.images.firstOrNull()?.url
            )
        }
    }
}

