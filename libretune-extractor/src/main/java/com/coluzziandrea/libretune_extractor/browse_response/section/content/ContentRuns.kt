package com.coluzziandrea.libretune_extractor.browse_response.section.content

import com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint.NavigationEndpoint
import kotlinx.serialization.Serializable


@Serializable
data class ContentRuns(
    val text: String,
    val navigationEndpoint: NavigationEndpoint? = null
)