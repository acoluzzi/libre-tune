package com.colux.libretune.data.sync

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

/** SharedPreferences-backed [SyncMetadataStore] for the Android app. */
@Singleton
class AndroidSyncMetadataStore @Inject constructor(
    @ApplicationContext context: Context,
) : SyncMetadataStore {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun setLocalChangedAt(collection: SyncCollection, timestamp: Long) {
        prefs.edit().putLong(localKey(collection), timestamp).apply()
    }

    override fun localChangedAt(collection: SyncCollection): Long =
        prefs.getLong(localKey(collection), 0L)

    override fun setRemoteUpdatedAt(collection: SyncCollection, timestamp: Long) {
        prefs.edit().putLong(remoteKey(collection), timestamp).apply()
    }

    override fun remoteUpdatedAt(collection: SyncCollection): Long =
        prefs.getLong(remoteKey(collection), 0L)

    override fun clear() {
        prefs.edit().clear().apply()
    }

    private fun localKey(collection: SyncCollection) = "${collection.key}_local_at"
    private fun remoteKey(collection: SyncCollection) = "${collection.key}_remote_at"

    private companion object {
        const val PREFS_NAME = "libretune_sync_metadata"
    }
}
