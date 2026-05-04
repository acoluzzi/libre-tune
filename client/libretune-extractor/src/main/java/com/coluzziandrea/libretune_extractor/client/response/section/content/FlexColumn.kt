package com.coluzziandrea.libretune_extractor.client.response.section.content

import kotlinx.serialization.Serializable


@Serializable
data class FlexColumnText(
    val runs: List<ContentRuns>? = null,
)

@Serializable
data class MusicResponsiveListItemFlexColumnRenderer(
    val text: FlexColumnText
)


@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer
)

@Serializable
data class MusicResponsiveListItemFixedColumnRenderer(
    val text: FlexColumnText
)

@Serializable
data class FixedColumn(
    val musicResponsiveListItemFixedColumnRenderer: MusicResponsiveListItemFixedColumnRenderer
)