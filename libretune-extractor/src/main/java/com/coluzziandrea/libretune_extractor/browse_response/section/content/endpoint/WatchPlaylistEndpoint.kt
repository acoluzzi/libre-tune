package com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint


import kotlinx.serialization.Serializable


@Serializable
data class WatchPlaylistEndpoint(
    val playlistId: String
)