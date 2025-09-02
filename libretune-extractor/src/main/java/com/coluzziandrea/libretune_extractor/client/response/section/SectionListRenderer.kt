package com.coluzziandrea.libretune_extractor.client.response.section

import com.coluzziandrea.libretune_extractor.client.response.section.content.SectionContent
import kotlinx.serialization.Serializable

@Serializable
data class SectionListRenderer(
    val contents: List<SectionContent>
)