package com.colux.libretune.data.sync

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

/**
 * Tracks per-collection timestamps used by [LibrarySyncOrchestrator] to
 * decide whether the local snapshot must be pushed to the backend.
 *
 * - [setLocalChangedAt] is called every time the user mutates the matching
 *   local collection (likes a song, saves an album, etc.).
 * - [setSyncedAt] is called after a successful push or pull.
 * - When `localChangedAt > syncedAt` the collection is "dirty" and must
 *   be pushed on the next batch run.
 */
@Singleton
class SyncMetadataStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setLocalChangedAt(collection: SyncCollection, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(localKey(collection), timestamp).apply()
    }

    fun localChangedAt(collection: SyncCollection): Long =
        prefs.getLong(localKey(collection), 0L)

    fun setSyncedAt(collection: SyncCollection, timestamp: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(syncedKey(collection), timestamp).apply()
    }

    fun syncedAt(collection: SyncCollection): Long =
        prefs.getLong(syncedKey(collection), 0L)

    fun isDirty(collection: SyncCollection): Boolean =
        localChangedAt(collection) > syncedAt(collection)

    /** Wipe all sync timestamps (e.g. on logout). */
    fun clear() {
        prefs.edit().clear().apply()
    }

    private fun localKey(collection: SyncCollection) = "${collection.key}_local_at"
    private fun syncedKey(collection: SyncCollection) = "${collection.key}_synced_at"

    private companion object {
        const val PREFS_NAME = "libretune_sync_metadata"
    }
}
