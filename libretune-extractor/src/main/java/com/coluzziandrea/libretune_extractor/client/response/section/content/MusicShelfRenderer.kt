package com.coluzziandrea.libretune_extractor.client.response.section.content

import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class MusicShelfRenderer(
    val title: ContentTitle? = null,
    val contents: List<SectionContent>,
    val bottomText: ContentTitle? = null,
    val bottomEndpoint: NavigationEndpoint? = null
)