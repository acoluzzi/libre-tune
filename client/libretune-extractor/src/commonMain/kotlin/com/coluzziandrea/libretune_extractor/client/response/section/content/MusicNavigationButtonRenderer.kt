package com.coluzziandrea.libretune_extractor.client.response.section.content

import com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint.BrowseEndpoint
import kotlinx.serialization.Serializable


@Serializable
data class ClickCommand(
    val browseEndpoint: BrowseEndpoint
)


@Serializable
data class MusicNavigationButtonRenderer(
    val buttonText: ContentTitle,
    val clickCommand: ClickCommand
)