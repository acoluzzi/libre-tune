package com.colux.libretune.data.local

import androidx.room.RoomDatabase

/**
 * Platform-agnostic wrapper for Room database transactions.
 *
 * - Android: delegates to `androidx.room.withTransaction` which participates
 *   in Room's full transaction/coroutine machinery.
 * - Desktop JVM: room-runtime-jvm does not expose `withTransaction`; the
 *   block is executed directly. Each individual DAO operation remains atomic;
 *   multi-step atomicity is best-effort until Room KMP exposes a public
 *   cross-platform transaction API.
 */
internal expect suspend fun <R> RoomDatabase.dbWithTransaction(block: suspend () -> R): R
