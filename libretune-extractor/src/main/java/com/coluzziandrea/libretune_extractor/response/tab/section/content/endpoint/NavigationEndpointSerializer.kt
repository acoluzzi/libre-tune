package com.coluzziandrea.libretune_extractor.response.tab.section.content.endpoint

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

object NavigationEndpointSerializer :
    JsonContentPolymorphicSerializer<NavigationEndpoint>(NavigationEndpoint::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<NavigationEndpoint> =
        when {
            "watchEndpoint" in element.jsonObject -> NavigationEndpoint.WatchNavigationEndpoint.serializer()

            "browseEndpoint" in element.jsonObject -> NavigationEndpoint.BrowseNavigationEndpoint.serializer()

            "watchPlaylistEndpoint" in element.jsonObject -> NavigationEndpoint.WatchPlaylistNavigationEndpoint.serializer()

            // Default case
            else -> NavigationEndpoint.EmptyNavigationEndpoint.serializer()
        }

}