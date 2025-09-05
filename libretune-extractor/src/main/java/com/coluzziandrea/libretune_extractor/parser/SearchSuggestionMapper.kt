package com.coluzziandrea.libretune_extractor.parser

import com.coluzziandrea.libretune_extractor.client.response.MultipleContentBrowseData
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.model.SearchSuggestion
import com.coluzziandrea.libretune_extractor.parser.mapper.toMusicItem

fun MultipleContentBrowseData.toSearchSuggestions(): List<SearchSuggestion> {

    val searchSuggestions = mutableListOf<SearchSuggestion>()
    contents?.forEach { suggestionContent ->
        suggestionContent.searchSuggestionsSectionRenderer?.contents?.forEach { rendererContent ->

            if (rendererContent is SectionContent.SearchSuggestionContent) {
                rendererContent.toSearchSuggestion()?.let { searchSuggestion ->
                    searchSuggestions.add(searchSuggestion)
                }
            }

            if (rendererContent is SectionContent.MusicResponsiveListItemContent) {
                rendererContent.toMusicItem()?.let {
                    searchSuggestions.add(
                        SearchSuggestion(
                            musicItem = it
                        )
                    )
                }
            }

        }

    }

    return searchSuggestions
}

fun SectionContent.SearchSuggestionContent.toSearchSuggestion(): SearchSuggestion? {
    val navigationEndpoint = searchSuggestionRenderer.navigationEndpoint
    if (navigationEndpoint !is NavigationEndpoint.SearchNavigationEndpoint) {
        return null
    }
    return SearchSuggestion(
        suggestion = navigationEndpoint.searchEndpoint.query
    )
}