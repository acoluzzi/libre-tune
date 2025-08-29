package com.coluzziandrea.libretune_extractor.browse_response.tab.section.content

import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class MusicShelfRenderer(
    val title: ContentTitle? = null,
    val contents: List<SectionContent>,
    val bottomEndpoint: NavigationEndpoint? = null
)