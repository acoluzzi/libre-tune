package com.coluzziandrea.libretune_extractor.response.tab.section.content

import kotlinx.serialization.Serializable


@Serializable
data class MusicThumbnailItem(
    val url: String,
    val width: Int,
    val height: Int
)


@Serializable
data class MusicThumbnail(
    val thumbnails: List<MusicThumbnailItem>
)


@Serializable
data class MusicThumbnailRenderer(
    val thumbnail: MusicThumbnail
)

@Serializable
data class Thumbnail(
    val musicThumbnailRenderer: MusicThumbnailRenderer
)