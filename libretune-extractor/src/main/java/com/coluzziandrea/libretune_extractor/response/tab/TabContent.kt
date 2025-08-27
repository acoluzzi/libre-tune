package com.coluzziandrea.libretune_extractor.response.tab

import com.coluzziandrea.libretune_extractor.response.tab.section.SectionListRenderer
import kotlinx.serialization.Serializable

@Serializable
data class TabContent(
    val sectionListRenderer: SectionListRenderer
)