package com.coluzziandrea.libretune_extractor.client.request

import kotlinx.serialization.Serializable

@Serializable
data class BrowseRequest(
    val browseId: String,
    val context: Context
)