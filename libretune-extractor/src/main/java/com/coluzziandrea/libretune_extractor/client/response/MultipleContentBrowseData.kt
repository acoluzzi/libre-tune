package com.coluzziandrea.libretune_extractor.client.response

import kotlinx.serialization.Serializable

@Serializable
data class MultipleContentBrowseData(
    val contents: List<BrowseDataContents>? = null,
)