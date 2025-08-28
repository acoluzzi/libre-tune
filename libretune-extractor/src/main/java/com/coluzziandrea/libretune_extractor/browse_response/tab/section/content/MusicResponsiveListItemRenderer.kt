package com.coluzziandrea.libretune_extractor.browse_response.tab.section.content

import kotlinx.serialization.Serializable

@Serializable
data class MusicResponsiveListItemRenderer(
    val thumbnail: Thumbnail? = null,
    val overlay: Overlay,
    val flexColumns: List<FlexColumn>,
)