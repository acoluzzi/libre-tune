package com.coluzziandrea.libretune_extractor.client.response.section.content

import kotlinx.serialization.Serializable

@Serializable
data class ContentTitle(
    val runs: List<ContentRuns>
)