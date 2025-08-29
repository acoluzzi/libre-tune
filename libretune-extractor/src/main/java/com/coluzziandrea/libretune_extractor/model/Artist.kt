package com.coluzziandrea.libretune_extractor.model

import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.CarouselContent
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint.NavigationEndpoint

data class Artist(
    val id: String,
    val name: String,
    val images: List<Image>
) {
    companion object {
        fun from(carouselItem: CarouselContent): Artist? {
            val item = carouselItem.musicTwoRowItemRenderer
            if (item == null) {
                return null
            }
            return Artist(
                id = (item.navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId,
                name = item.title.runs[0].text,
                images = item.thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.map {
                    Image(
                        url = it.url,
                        width = it.width,
                        height = it.height
                    )
                }
            )
        }
    }
}


