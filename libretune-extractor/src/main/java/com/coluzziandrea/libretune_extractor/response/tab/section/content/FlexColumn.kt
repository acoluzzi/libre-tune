package com.coluzziandrea.libretune_extractor.response.tab.section.content

import kotlinx.serialization.Serializable


@Serializable
data class FlexColumnText(
    val runs: List<ContentRuns>
)

@Serializable
data class MusicResponsiveListItemFlexColumnRenderer(
    val text: FlexColumnText
)


@Serializable
data class FlexColumn(
    val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer
)