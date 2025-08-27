package com.coluzziandrea.libretune_extractor.response

import com.coluzziandrea.libretune_extractor.response.tab.Tab
import kotlinx.serialization.Serializable

@Serializable
data class SingleColumnBrowseResultsRenderer(
    val tabs: List<Tab>
)