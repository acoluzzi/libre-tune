package com.coluzziandrea.libretune_extractor.browse_response.tab

import com.coluzziandrea.libretune_extractor.browse_response.tab.section.SectionListRenderer
import kotlinx.serialization.Serializable

@Serializable
data class TabContent(
    val sectionListRenderer: SectionListRenderer
)