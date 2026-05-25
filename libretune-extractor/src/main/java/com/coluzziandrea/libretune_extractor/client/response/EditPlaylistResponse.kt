package com.coluzziandrea.libretune_extractor.client.response

import kotlinx.serialization.Serializable

@Serializable
data class EditPlaylistResponse(
    val status: String? = null,
    val playlistEditResults: List<PlaylistEditResult> = emptyList()
)

@Serializable
data class PlaylistEditResult(
    val playlistEditVideoAddedResultData: PlaylistEditVideoAddedResultData? = null
)

@Serializable
data class PlaylistEditVideoAddedResultData(
    val videoId: String? = null,
    val setVideoId: String? = null
)

@Serializable
data class CreatePlaylistResponse(
    val playlistId: String? = null,
    val status: String? = null
)

@Serializable
data class DeletePlaylistResponse(
    val status: String? = null,
    val command: kotlinx.serialization.json.JsonElement? = null
)
