package com.coluzziandrea.libretune_extractor.browse_response.tab

import kotlinx.serialization.Serializable

@Serializable
data class TabRenderer(
    val content: TabContent
)

@Serializable
data class Tab(val tabRenderer: TabRenderer)