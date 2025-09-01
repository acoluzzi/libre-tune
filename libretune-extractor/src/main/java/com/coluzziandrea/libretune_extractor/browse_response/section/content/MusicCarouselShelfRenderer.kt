package com.coluzziandrea.libretune_extractor.browse_response.section.content

import com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class ButtonRenderer(
    val text: ContentTitle,
    val navigationEndpoint: NavigationEndpoint? = null
)

@Serializable
data class MoreContentButton(
    val buttonRenderer: ButtonRenderer? = null
)

@Serializable
data class MusicCarouselShelfBasicHeaderRenderer(
    val title: ContentTitle,
    val moreContentButton: MoreContentButton? = null
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
    val musicTwoRowItemRenderer: MusicTwoRowsItemRenderer? = null
)

@Serializable
data class MusicCarouselShelfRenderer(
    val header: CarouselHeader,
    val contents: List<CarouselContent>
)