package com.colux.libretune.data.sync

/**
 * The four collections that the LibreTune backend keeps in sync.
 * Each collection tracks an independent local-change timestamp and
 * last-successful-sync timestamp.
 */
enum class SyncCollection(val key: String) {
    LIKED_SONGS("liked_songs"),
    PLAYLISTS("playlists"),
    SAVED_ALBUMS("saved_albums"),
    SAVED_ARTISTS("saved_artists"),
}
