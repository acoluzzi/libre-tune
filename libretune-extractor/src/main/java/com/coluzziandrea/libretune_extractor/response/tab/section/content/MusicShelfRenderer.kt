package com.coluzziandrea.libretune_extractor.response.tab.section.content

import kotlinx.serialization.Serializable

@Serializable
data class MusicShelfRenderer(
    val title: ContentTitle,
    val contents: List<SectionContent>
)