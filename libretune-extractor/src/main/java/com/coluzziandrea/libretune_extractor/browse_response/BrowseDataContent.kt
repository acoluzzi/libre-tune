package com.coluzziandrea.libretune_extractor.browse_response

import com.coluzziandrea.libretune_extractor.browse_response.tab.Tab
import com.coluzziandrea.libretune_extractor.browse_response.tab.TabContent
import kotlinx.serialization.Serializable


@Serializable
data class SingleColumnBrowseResultsRenderer(
    val tabs: List<Tab>
)

@Serializable
data class TwoColumnBrowseResultsRenderer(
    val tabs: List<Tab>,
    val secondaryContents: TabContent? = null
)

@Serializable
data class BrowseDataContents(
    val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer? = null,
    val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer? = null
)