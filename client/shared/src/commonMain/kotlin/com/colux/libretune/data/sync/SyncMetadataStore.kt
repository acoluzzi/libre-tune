package com.colux.libretune.data.sync

/**
 * Tracks per-collection timestamps used by `LibrarySyncOrchestrator` for the
 * last-writer-wins decision on every batch run:
 *
 * - [localChangedAt] is the most recent local mutation timestamp, bumped from
 *   the repositories on every relevant write.
 * - [remoteUpdatedAt] is the timestamp the backend reported the last time we
 *   talked to it.
 *
 * The orchestrator compares these two numbers - together with whether the
 * server reports any timestamp at all - to decide whether the collection
 * must be pushed, pulled, or left alone.
 */
interface SyncMetadataStore {

    fun setLocalChangedAt(
        collection: SyncCollection,
        timestamp: Long = currentEpochMillis(),
    )

    fun localChangedAt(collection: SyncCollection): Long

    fun setRemoteUpdatedAt(collection: SyncCollection, timestamp: Long)

    fun remoteUpdatedAt(collection: SyncCollection): Long

    /** Wipe all sync timestamps (e.g. on logout). */
    fun clear()
}
