package com.coluzziandrea.libretune_extractor.response.tab.section.content

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
    data class MusicResponsiveListItemContent(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer
    ) : SectionContent

    @Serializable
    class EmptyContent : SectionContent

}