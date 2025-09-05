package com.coluzziandrea.libretune_extractor.client.response

import kotlinx.serialization.Serializable

@Serializable
data class SubscribeButtonRenderer(
    val channelId: String
)

@Serializable
data class SubscriptionButton(
    val subscribeButtonRenderer: SubscribeButtonRenderer
)


@Serializable
data class MusicImmersiveHeaderRenderer(
    val subscriptionButton: SubscriptionButton
)

@Serializable
data class BrowseDataHeader(
    val musicImmersiveHeaderRenderer: MusicImmersiveHeaderRenderer? = null
)