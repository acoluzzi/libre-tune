package com.coluzziandrea.libretune_extractor.browse_response.tab.section.content

import com.coluzziandrea.libretune_extractor.browse_response.shared.MusicThumbnail
import kotlinx.serialization.Serializable


@Serializable
data class MusicThumbnailRenderer(
    val thumbnail: MusicThumbnail
)

@Serializable
data class Thumbnail(
    val musicThumbnailRenderer: MusicThumbnailRenderer
)