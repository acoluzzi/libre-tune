package com.coluzziandrea.libretune_extractor.model


sealed interface TopResult {
    data class ArtistResult(val artist: Artist?) : TopResult
    data class SongResult(val song: Song?) : TopResult
    data class AlbumResult(val album: Playlist?) : TopResult
    data class PlaylistResult(val playlist: Playlist?) : TopResult
}

data class SearchResult(
    val topResults: List<TopResult>,
    val songs: List<Song>,
    val albums: List<Playlist>,
    val artists: List<Artist>,
    val playlists: List<Playlist>,
    val communityPlaylists: List<Playlist>,
)