package com.coluzziandrea.libretune_extractor.client.response.section.content

import kotlinx.serialization.Serializable


@Serializable
data class GridItem(
    val musicTwoRowItemRenderer: MusicTwoRowsItemRenderer? = null,
    val musicNavigationButtonRenderer: MusicNavigationButtonRenderer? = null
)

@Serializable
data class GridItemHeaderRenderer(
    val title: ContentTitle
)

@Serializable
data class GridItemHeader(
    val gridHeaderRenderer: GridItemHeaderRenderer
)

@Serializable
data class GridRenderer(
    val items: List<GridItem>,
    val header: GridItemHeader? = null
)