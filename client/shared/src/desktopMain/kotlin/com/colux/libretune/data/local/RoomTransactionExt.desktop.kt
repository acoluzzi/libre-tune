package com.colux.libretune.data.local

import androidx.room.RoomDatabase

// room-runtime-jvm does not expose withTransaction as a public KMP API.
// Individual DAO operations remain atomic; this shim runs the block directly.
internal actual suspend fun <R> RoomDatabase.dbWithTransaction(block: suspend () -> R): R =
    block()
