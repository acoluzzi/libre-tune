package com.coluzziandrea.libretune_extractor.response

import kotlinx.serialization.Serializable

@Serializable
data class Contents(
    val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer
)