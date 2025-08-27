package com.coluzziandrea.libretune_extractor.response.tab.section.content

import kotlinx.serialization.Serializable

@Serializable
data class MusicResponsiveListItemRenderer(
    val thumbnail: Thumbnail,
    val overlay: Overlay,
    val flexColumns: List<FlexColumn>,
)