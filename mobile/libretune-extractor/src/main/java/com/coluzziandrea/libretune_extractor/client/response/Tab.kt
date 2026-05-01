package com.coluzziandrea.libretune_extractor.client.response

import com.coluzziandrea.libretune_extractor.client.response.section.SectionListRenderer
import kotlinx.serialization.Serializable

@Serializable
data class TabContent(
    val sectionListRenderer: SectionListRenderer
)

@Serializable
data class TabRenderer(
    val content: TabContent
)

@Serializable
data class Tab(val tabRenderer: TabRenderer)