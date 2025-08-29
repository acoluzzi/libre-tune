package com.coluzziandrea.libretune_extractor.browse_response.tab.section.content

import kotlinx.serialization.Serializable

@Serializable
data class MusicPlaylistShelfRenderer(
    val title: ContentTitle? = null,
    val contents: List<SectionContent>
)