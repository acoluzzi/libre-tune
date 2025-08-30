package com.colux.libretune.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.colux.libretune.data.local.dao.PlaylistDao
import com.colux.libretune.data.local.entity.PlaylistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Provider

class DatabaseCallback(
    private val playlistDao: Provider<PlaylistDao>,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // Launch a coroutine to perform the database insertion off the main thread
        scope.launch {
            val likedSongsPlaylist = PlaylistEntity(
                playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
                name = DatabaseConstants.LIKED_SONGS_PLAYLIST_NAME,
                isLocal = true // It's a local, user-specific playlist
            )
            // Use the DAO to insert the default playlist
            playlistDao.get().insertPlaylist(likedSongsPlaylist)
        }
    }
}