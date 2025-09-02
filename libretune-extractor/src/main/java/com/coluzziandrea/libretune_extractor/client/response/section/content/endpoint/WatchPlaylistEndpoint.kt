package com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint


import kotlinx.serialization.Serializable


@Serializable
data class WatchPlaylistEndpoint(
    val playlistId: String
)