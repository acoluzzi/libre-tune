package com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint

import kotlinx.serialization.Serializable

@Serializable
data class WatchEndpointMusicConfig(
    val musicVideoType: String? = null
)

@Serializable
data class WatchEndpointSupportedConfig(
    val watchEndpointMusicConfig: WatchEndpointMusicConfig
)


@Serializable
data class WatchEndpoint(
    val videoId: String,
    val playlistId: String? = null,
    val watchEndpointMusicSupportedConfigs: WatchEndpointSupportedConfig? = null
)