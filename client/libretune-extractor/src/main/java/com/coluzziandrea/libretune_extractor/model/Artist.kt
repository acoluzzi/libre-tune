package com.coluzziandrea.libretune_extractor.model

import com.coluzziandrea.libretune_extractor.client.response.section.content.CarouselContent
import com.coluzziandrea.libretune_extractor.client.response.section.content.MusicCardShelfRenderer
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint

data class Artist(
    val node: MusicNode,
    val images: List<Image> = emptyList()
) {
    val name: String
        get() = node.name

    val id: String
        get() = node.id

    constructor(
        id: String,
        name: String,
        images: List<Image> = emptyList()
    ) : this(
        node = MusicNode(
            id = id,
            name = name
        ),
        images = images
    )

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

        fun from(item: MusicCardShelfRenderer?): Artist? {
            if (item == null) {
                return null
            }
            return Artist(
                id = (item.title.runs[0].navigationEndpoint as NavigationEndpoint.BrowseNavigationEndpoint).browseEndpoint.browseId,
                name = item.title.runs[0].text,
                images = item.thumbnail.musicThumbnailRenderer.thumbnail.thumbnails.map {
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


