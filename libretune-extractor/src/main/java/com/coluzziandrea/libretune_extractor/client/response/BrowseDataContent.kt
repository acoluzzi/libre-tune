package com.coluzziandrea.libretune_extractor.client.response

import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
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
data class TabbedSearchResultsRenderer(
    val tabs: List<Tab>
)

@Serializable
data class SearchSuggestionsSectionRenderer(
    val contents: List<SectionContent>
)

@Serializable
data class BrowseDataContents(
    val singleColumnBrowseResultsRenderer: SingleColumnBrowseResultsRenderer? = null,
    val twoColumnBrowseResultsRenderer: TwoColumnBrowseResultsRenderer? = null,
    val tabbedSearchResultsRenderer: TabbedSearchResultsRenderer? = null,
    val searchSuggestionsSectionRenderer: SearchSuggestionsSectionRenderer? = null
)