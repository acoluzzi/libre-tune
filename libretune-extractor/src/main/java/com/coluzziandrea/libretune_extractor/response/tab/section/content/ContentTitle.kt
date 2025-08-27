package com.coluzziandrea.libretune_extractor.response.tab.section.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentTitle(
    val runs: List<ContentRuns>
)