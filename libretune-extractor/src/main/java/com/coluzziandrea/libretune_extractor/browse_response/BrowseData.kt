package com.coluzziandrea.libretune_extractor.browse_response

import kotlinx.serialization.Serializable


@Serializable
data class BrowseData(
    val contents: BrowseDataContents,
    val microformat: Microformat? = null,
)