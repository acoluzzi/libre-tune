package com.coluzziandrea.libretune_extractor.response.tab.section.content

import com.coluzziandrea.libretune_extractor.response.tab.section.content.endpoint.WatchEndpoint
import kotlinx.serialization.Serializable


@Serializable
data class PlayNavigationEndpoint(
    val watchEndpoint: WatchEndpoint
)

@Serializable
data class MusicPlayButtonRenderer(
    val playNavigationEndpoint: PlayNavigationEndpoint,
)