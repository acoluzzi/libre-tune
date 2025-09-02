package com.coluzziandrea.libretune_extractor.client.response.section.content.endpoint

import kotlinx.serialization.Serializable

@Serializable(with = NavigationEndpointSerializer::class)
sealed interface NavigationEndpoint {

    @Serializable
    data class WatchNavigationEndpoint(
        val watchEndpoint: WatchEndpoint
    ) : NavigationEndpoint

    @Serializable
    data class BrowseNavigationEndpoint(
        val browseEndpoint: BrowseEndpoint
    ) : NavigationEndpoint

    @Serializable
    data class QueueAddNavigationEndpoint(
        val queueAddEndpoint: QueueAddEndpoint
    ) : NavigationEndpoint


    @Serializable
    data class WatchPlaylistNavigationEndpoint(
        val watchPlaylistEndpoint: WatchPlaylistEndpoint
    ) : NavigationEndpoint

    @Serializable
    data class SearchNavigationEndpoint(
        val searchEndpoint: SearchEndpoint
    ) : NavigationEndpoint


    @Serializable
    class EmptyNavigationEndpoint : NavigationEndpoint
}
