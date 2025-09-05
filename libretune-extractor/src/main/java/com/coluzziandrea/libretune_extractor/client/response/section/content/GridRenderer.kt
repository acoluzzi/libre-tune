package com.coluzziandrea.libretune_extractor.client.response.section.content

import kotlinx.serialization.Serializable


@Serializable
data class GridItem(
    val musicTwoRowItemRenderer: MusicTwoRowsItemRenderer
)

@Serializable
data class GridRenderer(
    val items: List<GridItem>
)