package com.coluzziandrea.libretune_extractor.client.request

import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionRequest(
    val input: String,
    val context: Context
)