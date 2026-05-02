package com.colux.libretune.data.model


sealed interface HomeFeedItem {
    data class RelatedArtistsCarousel(
        val artist: Artist,
        val artists: List<Artist>
    ) : HomeFeedItem

    data class RelatedPlaylistsCarousel(
        val album: Playlist,
        val playlists: List<Playlist>
    ) : HomeFeedItem
}

