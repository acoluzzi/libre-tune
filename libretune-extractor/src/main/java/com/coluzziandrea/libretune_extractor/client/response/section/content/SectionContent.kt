package com.coluzziandrea.libretune_extractor.client.response.section.content

import kotlinx.serialization.Serializable

@Serializable(with = SectionContentSerializer::class)
sealed interface SectionContent {


    @Serializable
    data class MusicShelfContent(
        val musicShelfRenderer: MusicShelfRenderer
    ) : SectionContent


    @Serializable
    data class MusicCarouselContent(
        val musicCarouselShelfRenderer: MusicCarouselShelfRenderer
    ) : SectionContent

    @Serializable
    data class MusicPlaylistShelfContent(
        val musicPlaylistShelfRenderer: MusicPlaylistShelfRenderer
    ) : SectionContent


    @Serializable
    data class MusicResponsiveListItemContent(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer
    ) : SectionContent

    @Serializable
    data class MusicResponsiveHeaderContent(
        val musicResponsiveHeaderRenderer: MusicResponsiveHeaderRenderer
    ) : SectionContent

    @Serializable
    data class MusicCardShelfContent(
        val musicCardShelfRenderer: MusicCardShelfRenderer
    ) : SectionContent

    @Serializable
    data class SearchSuggestionContent(
        val searchSuggestionRenderer: SearchSuggestionRenderer
    ) : SectionContent

    @Serializable
    data class GridContent(
        val gridRenderer: GridRenderer
    ) : SectionContent

    @Serializable
    class EmptyContent : SectionContent

}