package com.coluzziandrea.libretune_extractor.parser.mapper

import com.coluzziandrea.libretune_extractor.client.response.BrowseData
import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import com.coluzziandrea.libretune_extractor.model.Discography

fun BrowseData.toDiscography(): Discography? {

    val albumsContentItem =
        contents.singleColumnBrowseResultsRenderer?.tabs
            ?.firstOrNull()?.tabRenderer?.content
            ?.sectionListRenderer?.contents?.firstOrNull()

    if (albumsContentItem is SectionContent.GridContent) {
        albumsContentItem.gridRenderer.items.mapNotNull {
            it.musicTwoRowItemRenderer.toPlaylist()
        }.let {
            return Discography(
                albums = it
            )
        }
    } else {
        return null
    }

}