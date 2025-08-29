package com.coluzziandrea.libretune_extractor.model

import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.CarouselContent
import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.endpoint.NavigationEndpoint

data class Playlist(val id: String, val name: String, val images: List<Image>) {
    companion object {
        fun from(carouselItem: CarouselContent): Playlist? {
            val item = carouselItem.musicTwoRowItemRenderer

            if (item == null) {
                return null
            }

            val id =
                (item.navigationEndpoint as? NavigationEndpoint.BrowseNavigationEndpoint)?.browseEndpoint?.browseId


            val name =
                carouselItem.musicTwoRowItemRenderer.title.runs.firstOrNull()?.text

            if (id == null || name == null || id.isEmpty() || name.isEmpty()) {
                return null
            }

            val images = item.thumbnailRenderer.musicThumbnailRenderer.thumbnail.thumbnails.map {
                Image(
                    url = it.url,
                    width = it.width,
                    height = it.height
                )
            }


            return Playlist(
                id = id,
                name = name,
                images = images
            )
        }
    }
}