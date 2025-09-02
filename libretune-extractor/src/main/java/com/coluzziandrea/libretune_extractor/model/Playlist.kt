package com.coluzziandrea.libretune_extractor.model

import com.coluzziandrea.libretune_extractor.client.response.section.content.CarouselContent
import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.NavigationEndpoint

data class Playlist(
    val node: MusicNode,
    val images: List<Image>,
    val artists: List<Artist>? = null
) {

    val name: String
        get() = node.name

    val id: String
        get() = node.id

    constructor(
        id: String,
        name: String,
        images: List<Image>,
        artists: List<Artist>? = null
    ) : this(
        node = MusicNode(
            id = id,
            name = name
        ),
        images = images,
        artists = artists
    )

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