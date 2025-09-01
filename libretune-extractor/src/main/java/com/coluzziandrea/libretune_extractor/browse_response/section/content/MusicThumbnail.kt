package com.coluzziandrea.libretune_extractor.browse_response.section.content

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