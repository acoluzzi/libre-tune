package com.coluzziandrea.libretune_extractor.browse_response.section.content.menu

import kotlinx.serialization.Serializable


@Serializable
data class MenuRenderer(
    val items: List<MenuItem>
)


@Serializable
data class Menu(
    val menuRenderer: MenuRenderer
)