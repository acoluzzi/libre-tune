package com.coluzziandrea.libretune_extractor.browse_response.section.content

import kotlinx.serialization.Serializable


@Serializable
data class MusicThumbnailRenderer(
    val thumbnail: MusicThumbnail
)

@Serializable
data class Thumbnail(
    val musicThumbnailRenderer: MusicThumbnailRenderer
)