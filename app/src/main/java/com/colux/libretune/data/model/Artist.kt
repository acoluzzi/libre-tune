package com.colux.libretune.data.model

import com.coluzziandrea.libretune_extractor.model.Artist as ExtractorArtist

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String
) {
    companion object {
        fun from(raw: ExtractorArtist): Artist {
            return Artist(
                id = raw.id,
                name = raw.name,
                imageUrl = raw.images.firstOrNull()?.url ?: ""
            )
        }
    }
}

