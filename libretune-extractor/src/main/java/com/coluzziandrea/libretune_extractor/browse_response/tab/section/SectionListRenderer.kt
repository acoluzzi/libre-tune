package com.coluzziandrea.libretune_extractor.browse_response.tab.section

import com.coluzziandrea.libretune_extractor.browse_response.tab.section.content.SectionContent
import kotlinx.serialization.Serializable

@Serializable
data class SectionListRenderer(
    val contents: List<SectionContent>
)