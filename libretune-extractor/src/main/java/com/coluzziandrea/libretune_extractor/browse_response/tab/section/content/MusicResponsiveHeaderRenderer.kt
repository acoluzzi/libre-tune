package com.coluzziandrea.libretune_extractor.browse_response.tab.section.content

import kotlinx.serialization.Serializable

@Serializable
data class MusicResponsiveHeaderRenderer(
    val thumbnail: Thumbnail,
    val title: ContentTitle,
    val subtitle: ContentTitle,
    val secondSubtitle: ContentTitle,
    val straplineTextOne: ContentTitle? = null,
)