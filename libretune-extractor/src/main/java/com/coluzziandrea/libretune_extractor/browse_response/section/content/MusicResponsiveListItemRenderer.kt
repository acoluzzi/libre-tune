package com.coluzziandrea.libretune_extractor.browse_response.section.content

import com.coluzziandrea.libretune_extractor.browse_response.section.content.endpoint.NavigationEndpoint
import com.coluzziandrea.libretune_extractor.browse_response.section.content.menu.Menu
import kotlinx.serialization.Serializable

@Serializable
data class MusicResponsiveListItemRenderer(
    val thumbnail: Thumbnail? = null,
    val overlay: Overlay? = null,
    val flexColumns: List<FlexColumn>,
    val menu: Menu? = null,
    val navigationEndpoint: NavigationEndpoint? = null,
)