package com.coluzziandrea.libretune_extractor.browse_response.section.content

import kotlinx.serialization.Serializable

@Serializable
class OverlayContent(
    val musicPlayButtonRenderer: MusicPlayButtonRenderer
)

@Serializable
class MusicItemThumbnailOverlayRenderer(
    val content: OverlayContent
)

@Serializable
data class Overlay(
    val musicItemThumbnailOverlayRenderer: MusicItemThumbnailOverlayRenderer
)