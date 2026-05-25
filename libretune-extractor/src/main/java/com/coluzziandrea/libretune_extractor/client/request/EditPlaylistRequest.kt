package com.coluzziandrea.libretune_extractor.client.request

import kotlinx.serialization.Serializable

@Serializable
data class EditPlaylistAction(
    val action: String,
    val addedVideoId: String? = null,
    val removedVideoId: String? = null,
    val setVideoId: String? = null
) {
    companion object {
        fun add(videoId: String) = EditPlaylistAction(
            action = "ACTION_ADD_VIDEO",
            addedVideoId = videoId
        )

        fun remove(videoId: String, setVideoId: String) = EditPlaylistAction(
            action = "ACTION_REMOVE_VIDEO",
            removedVideoId = videoId,
            setVideoId = setVideoId
        )
    }
}

@Serializable
data class EditPlaylistRequest(
    val playlistId: String,
    val actions: List<EditPlaylistAction>,
    val context: Context
)
