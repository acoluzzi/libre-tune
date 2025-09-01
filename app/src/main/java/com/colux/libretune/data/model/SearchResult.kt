package com.colux.libretune.data.model


data class SearchResult(
    val topSongs: List<Song>,
    val topAlbums: List<Playlist>,
    val topArtists: List<Artist>,
    val songs: List<Song>,
    val albums: List<Playlist>,
    val artists: List<Artist>,
    val playlists: List<Playlist>,
    val communityPlaylists: List<Playlist>,
) {
    val hasTopResults: Boolean
        get() = topSongs.isNotEmpty() || topAlbums.isNotEmpty() || topArtists.isNotEmpty()


    val isEmpty: Boolean
        get() = topSongs.isEmpty() && topAlbums.isEmpty() && topArtists.isEmpty() &&
                songs.isEmpty() && albums.isEmpty() && artists.isEmpty() &&
                playlists.isEmpty() && communityPlaylists.isEmpty()
}