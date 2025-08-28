package com.coluzziandrea.libretune_extractor.browse_response

import com.coluzziandrea.libretune_extractor.browse_response.shared.MusicThumbnail
import kotlinx.serialization.Serializable


@Serializable
data class MicroformatDataRenderer(
    val urlCanonical: String,
    val title: String,
    val description: String,
    val thumbnail: MusicThumbnail
)


@Serializable
data class Microformat(
    val microformatDataRenderer: MicroformatDataRenderer
)
