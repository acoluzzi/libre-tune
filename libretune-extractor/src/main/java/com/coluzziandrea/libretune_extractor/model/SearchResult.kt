package com.coluzziandrea.libretune_extractor.model


sealed interface GenericMusicItem {
    data class ArtistResult(val artist: Artist?) : GenericMusicItem
    data class SongResult(val song: Song?) : GenericMusicItem
    data class AlbumResult(val album: Playlist?) : GenericMusicItem
    data class PlaylistResult(val playlist: Playlist?) : GenericMusicItem
}

data class SearchResult(
    val genericMusicItems: List<GenericMusicItem>,
    val songs: List<Song>,
    val albums: List<Playlist>,
    val artists: List<Artist>,
    val playlists: List<Playlist>,
    val communityPlaylists: List<Playlist>,
)