package com.colux.libretune.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import java.io.File

/**
 * Desktop entry point that produces a partially-configured [AppDatabase] builder.
 * Uses [XerialJdbcSQLiteDriver] (org.xerial sqlite-jdbc) to avoid JNI/glibc conflicts
 * that [BundledSQLiteDriver] causes on some Linux/JBR configurations.
 */
fun createAppDatabaseBuilder(dbFile: File): RoomDatabase.Builder<AppDatabase> {
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
        .setDriver(XerialJdbcSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}
