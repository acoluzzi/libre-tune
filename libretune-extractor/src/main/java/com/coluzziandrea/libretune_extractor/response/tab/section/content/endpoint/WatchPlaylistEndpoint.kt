package com.coluzziandrea.libretune_extractor.response.tab.section.content.endpoint


import kotlinx.serialization.Serializable


@Serializable
data class WatchPlaylistEndpoint(
    val playlistId: String
)