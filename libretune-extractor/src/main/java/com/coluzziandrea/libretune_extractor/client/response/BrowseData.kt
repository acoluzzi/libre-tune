package com.coluzziandrea.libretune_extractor.client.response

import kotlinx.serialization.Serializable


@Serializable
data class BrowseData(
    val contents: BrowseDataContents,
    val microformat: Microformat? = null,
    val header: BrowseDataHeader? = null,
)