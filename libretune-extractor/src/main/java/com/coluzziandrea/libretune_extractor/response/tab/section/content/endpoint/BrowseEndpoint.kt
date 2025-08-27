package com.coluzziandrea.libretune_extractor.response.tab.section.content.endpoint

import kotlinx.serialization.Serializable

@Serializable
data class BrowseEndpointMusicConfig(
    val pageType: String
)

@Serializable
data class BrowseEndpointSupportedConfig(
    val browseEndpointContextMusicConfig: WatchEndpointMusicConfig
)


@Serializable
data class BrowseEndpoint(
    val browseId: String,
    val browseEndpointContextSupportedConfigs: BrowseEndpointSupportedConfig
)