package com.coluzziandrea.libretune_extractor.client.request

import kotlinx.serialization.Serializable

@Serializable
data class CreatePlaylistRequest(
    val title: String,
    val description: String = "",
    val privacyStatus: String = "PRIVATE",
    val videoIds: List<String> = emptyList(),
    val context: Context
)

@Serializable
data class DeletePlaylistRequest(
    val playlistId: String,
    val context: Context
)
