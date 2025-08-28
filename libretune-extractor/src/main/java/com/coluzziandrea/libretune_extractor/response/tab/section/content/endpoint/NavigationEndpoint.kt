package com.coluzziandrea.libretune_extractor.response.tab.section.content.endpoint

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
    data class WatchPlaylistNavigationEndpoint(
        val watchPlaylistEndpoint: WatchPlaylistEndpoint
    ) : NavigationEndpoint


    @Serializable
    class EmptyNavigationEndpoint : NavigationEndpoint
}