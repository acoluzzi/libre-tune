package com.colux.libretune.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import java.io.File

/**
 * Desktop entry point that produces a partially-configured [AppDatabase] builder.
 * Callers pass the file the SQLite database should live in (e.g. inside the
 * user's home / data directory).
 */
fun createAppDatabaseBuilder(dbFile: File): RoomDatabase.Builder<AppDatabase> {
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}
