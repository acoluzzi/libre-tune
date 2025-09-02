package com.coluzziandrea.libretune_extractor.client.response.section.content

import kotlinx.serialization.Serializable

@Serializable
data class MusicCardShelfRenderer(
    val thumbnail: Thumbnail,
    val title: ContentTitle,
    val subtitle: ContentTitle? = null,
    val contents: List<SectionContent>? = null,
)