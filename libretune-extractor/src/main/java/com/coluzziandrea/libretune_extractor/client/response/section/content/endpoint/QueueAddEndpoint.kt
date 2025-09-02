package com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint

import kotlinx.serialization.Serializable


@Serializable
data class QueueTarget(
    val videoId: String? = null,
    val playlistId: String? = null,
    val onEmptyQueue: NavigationEndpoint? = null
)

@Serializable
data class QueueAddEndpoint(
    val queueTarget: QueueTarget
)