package com.colux.libretune.data.local

import androidx.room.RoomDatabase

// room-ktx's withTransaction calls beginTransaction() → getOpenHelper(), which requires a
// SupportSQLiteOpenHelper.Factory.  BundledSQLiteDriver does not provide one, so it throws.
// Individual Room DAO calls are each implicitly atomic, so running the block directly is safe
// for our write-behind cache use case (mirrors the desktop actual).
internal actual suspend fun <R> RoomDatabase.dbWithTransaction(block: suspend () -> R): R =
    block()
