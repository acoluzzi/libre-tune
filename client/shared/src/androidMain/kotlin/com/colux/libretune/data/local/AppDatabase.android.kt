package com.colux.libretune.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

/**
 * Android entry point that produces a partially-configured [AppDatabase] builder.
 * Hilt's `DatabaseModule` calls this and adds the `DatabaseCallback` before
 * building.
 */
fun createAppDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = appContext.getDatabasePath("libretune_db").absolutePath,
    )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
}
