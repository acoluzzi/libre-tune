package com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint

import kotlinx.serialization.Serializable

@Serializable
data class BrowseEndpointMusicConfig(
    val pageType: String
)

@Serializable
data class BrowseEndpointSupportedConfig(
    val browseEndpointContextMusicConfig: BrowseEndpointMusicConfig
)


@Serializable
data class BrowseEndpoint(
    val browseId: String,
    val params: String? = null,
    val browseEndpointContextSupportedConfigs: BrowseEndpointSupportedConfig? = null
)