package com.colux.libretune.data.sync

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Tracks per-collection timestamps used by [LibrarySyncOrchestrator] for the
 * last-writer-wins decision on every batch run:
 *
 * - [localChangedAt] is the most recent local mutation timestamp; bumped from
 *   the repositories on every relevant write.
 * - [remoteUpdatedAt] is the timestamp the backend reported the last time we
 *   talked to it. After a successful pull it equals the server's value;
 *   after a successful push it equals the timestamp the client uploaded.
 *
 * The orchestrator compares these two numbers — together with whether the
 * server reports any timestamp at all — to decide whether the collection
 * must be pushed, pulled, or left alone.
 */
@Singleton
class SyncMetadataStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setLocalChangedAt(
        collection: SyncCollection,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        prefs.edit().putLong(localKey(collection), timestamp).apply()
    }

    fun localChangedAt(collection: SyncCollection): Long =
        prefs.getLong(localKey(collection), 0L)

    fun setRemoteUpdatedAt(collection: SyncCollection, timestamp: Long) {
        prefs.edit().putLong(remoteKey(collection), timestamp).apply()
    }

    fun remoteUpdatedAt(collection: SyncCollection): Long =
        prefs.getLong(remoteKey(collection), 0L)

    /** Wipe all sync timestamps (e.g. on logout). */
    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun localKey(collection: SyncCollection) = "${collection.key}_local_at"
    private fun remoteKey(collection: SyncCollection) = "${collection.key}_remote_at"

    private companion object {
        const val PREFS_NAME = "libretune_sync_metadata"
    }
}
