package com.coluzziandrea.libretune_extractor.client.response.section.content

import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionRenderer(
    val suggestion: ContentTitle,
    val navigationEndpoint: NavigationEndpoint? = null
)