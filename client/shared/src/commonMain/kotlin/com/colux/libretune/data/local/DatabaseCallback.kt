package com.colux.libretune.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import com.colux.libretune.data.local.dao.PlaylistDao
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.PlaylistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Seeds the LIKED_SONGS playlist when the database is first created. The DAO
 * is supplied as a lambda to avoid the AppDatabase <-> PlaylistDao cycle: the
 * callback is needed to build the database, which in turn owns the DAO.
 */
class DatabaseCallback(
    private val playlistDao: () -> PlaylistDao,
    private val scope: CoroutineScope,
) : RoomDatabase.Callback() {

    override fun onCreate(connection: SQLiteConnection) {
        super.onCreate(connection)

        scope.launch {
            val likedSongsPlaylist = PlaylistEntity(
                playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
                name = DatabaseConstants.LIKED_SONGS_PLAYLIST_NAME,
                images = emptyList(),
                isLocal = true,
                type = AlbumType.PLAYLIST,
            )
            playlistDao().upsert(likedSongsPlaylist)
        }
    }
}