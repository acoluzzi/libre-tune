package com.colux.libretune.data.local.join

import androidx.room.Entity

/**
 * Sync state of a single song inside a synced playlist.
 *
 * - [SYNCED]: in step with YouTube Music.
 * - [PENDING_ADD]: added locally, push to YT Music still owed.
 * - [PENDING_REMOVE]: removed locally, delete-on-YT-Music still owed.
 */
enum class PlaylistSongSyncState { SYNCED, PENDING_ADD, PENDING_REMOVE }

@Entity(tableName = "playlist_song_cross_ref", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String,
    /** YT Music per-occurrence identifier; required to remove the row from YT Music. */
    val setVideoId: String? = null,
    val syncState: PlaylistSongSyncState = PlaylistSongSyncState.SYNCED
)
