package com.colux.libretune.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import com.colux.libretune.data.local.dao.PlaylistDao
import com.colux.libretune.data.local.entity.AlbumType
import com.colux.libretune.data.local.entity.PlaylistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Provider

class DatabaseCallback(
    private val playlistDao: Provider<PlaylistDao>,
    private val scope: CoroutineScope
) : RoomDatabase.Callback() {

    override fun onCreate(connection: SQLiteConnection) {
        super.onCreate(connection)

        scope.launch {
            val likedSongsPlaylist = PlaylistEntity(
                playlistId = DatabaseConstants.LIKED_SONGS_PLAYLIST_ID,
                name = DatabaseConstants.LIKED_SONGS_PLAYLIST_NAME,
                images = emptyList(),
                isLocal = true,
                type = AlbumType.PLAYLIST
            )
            playlistDao.get().upsert(likedSongsPlaylist)
        }
    }
}