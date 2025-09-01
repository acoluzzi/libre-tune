package com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint

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
    val videoId: String? = null,
    val playlistId: String? = null,
    val watchEndpointMusicSupportedConfigs: WatchEndpointSupportedConfig? = null
)

