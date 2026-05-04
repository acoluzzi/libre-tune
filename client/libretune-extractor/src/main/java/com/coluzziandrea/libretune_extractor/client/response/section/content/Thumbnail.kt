package com.coluzziandrea.libretune_extractor.client.response.section.content

import kotlinx.serialization.Serializable


@Serializable
data class MusicThumbnailRenderer(
    val thumbnail: MusicThumbnail
)

@Serializable
data class Thumbnail(
    val musicThumbnailRenderer: MusicThumbnailRenderer
)