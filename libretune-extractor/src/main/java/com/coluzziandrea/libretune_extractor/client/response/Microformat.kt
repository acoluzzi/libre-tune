package com.coluzziandrea.libretune_extractor.client.response

import com.coluzziandrea.libretune_extractor.client.response.section.content.MusicThumbnail
import kotlinx.serialization.Serializable


@Serializable
data class MicroformatDataRenderer(
    val urlCanonical: String,
    val title: String,
    val description: String? = null,
    val thumbnail: MusicThumbnail
)


@Serializable
data class Microformat(
    val microformatDataRenderer: MicroformatDataRenderer
)
