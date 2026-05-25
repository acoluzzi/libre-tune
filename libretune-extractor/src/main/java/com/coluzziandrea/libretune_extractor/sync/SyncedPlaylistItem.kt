package com.coluzziandrea.libretune_extractor.sync

/**
 * A single track inside a YouTube Music playlist as returned by the
 * authenticated `browse VL<playlistId>` call.
 *
 * `setVideoId` is the unique identifier of *this occurrence* inside the
 * playlist — you need it (not the videoId) to ask YT Music to remove the
 * track, since a playlist can contain the same video multiple times.
 */
data class SyncedPlaylistItem(
    val videoId: String,
    val setVideoId: String?,
    val title: String?,
    val artistNames: String?,
    val albumName: String?
)

data class RemotePlaylistSnapshot(
    val playlistId: String,
    val title: String?,
    val items: List<SyncedPlaylistItem>
)

data class LibraryPlaylistSummary(
    val playlistId: String,
    val title: String
)
