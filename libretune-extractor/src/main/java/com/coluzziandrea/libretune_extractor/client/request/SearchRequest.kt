package com.coluzziandrea.libretune_extractor.client.request

import kotlinx.serialization.Serializable

@Serializable
data class SearchRequest(
    val query: String,
    val context: Context
)