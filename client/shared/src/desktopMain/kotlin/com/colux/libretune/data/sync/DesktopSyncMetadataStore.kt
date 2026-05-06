package com.colux.libretune.data.sync

import java.util.prefs.Preferences

class DesktopSyncMetadataStore : SyncMetadataStore {

    private val prefs: Preferences =
        Preferences.userRoot().node("com/colux/libretune/sync")

    override fun setLocalChangedAt(collection: SyncCollection, timestamp: Long) {
        prefs.putLong(localKey(collection), timestamp)
        prefs.flush()
    }

    override fun localChangedAt(collection: SyncCollection): Long =
        prefs.getLong(localKey(collection), 0L)

    override fun setRemoteUpdatedAt(collection: SyncCollection, timestamp: Long) {
        prefs.putLong(remoteKey(collection), timestamp)
        prefs.flush()
    }

    override fun remoteUpdatedAt(collection: SyncCollection): Long =
        prefs.getLong(remoteKey(collection), 0L)

    override fun clear() {
        prefs.clear()
        prefs.flush()
    }

    private fun localKey(collection: SyncCollection): String = "${collection.key}_local_at"
    private fun remoteKey(collection: SyncCollection): String = "${collection.key}_remote_at"
}
