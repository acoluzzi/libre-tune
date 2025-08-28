package com.coluzziandrea.libretune_extractor.browse_response.tab.section.content

import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class MusicCarouselShelfBasicHeaderRenderer(
    val title: ContentTitle
)


@Serializable
data class CarouselHeader(
    val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer
)

@Serializable
data class MusicTwoRowsItemRenderer(
    val thumbnailRenderer: Thumbnail,
    val title: ContentTitle,
    val subtitle: ContentTitle,
    val navigationEndpoint: NavigationEndpoint
)

@Serializable
data class CarouselContent(
    val musicTwoRowItemRenderer: MusicTwoRowsItemRenderer
)

@Serializable
data class MusicCarouselShelfRenderer(
    val header: CarouselHeader,
    val contents: List<CarouselContent>
)