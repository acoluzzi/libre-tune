package com.coluzziandrea.libretune_extractor.response.tab

import kotlinx.serialization.Serializable

@Serializable
data class TabRenderer(
    val content: TabContent
)