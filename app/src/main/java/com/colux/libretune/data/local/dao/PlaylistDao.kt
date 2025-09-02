package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.join.PlaylistSongCrossRef

@Dao
interface PlaylistDao {
    /**
     * Inserts a new playlist. Used for creating the default "Liked Songs"
     * playlist and for when the user creates a new one.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    /**
     * Adds a song to a playlist by inserting an entry into the join table.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(join: PlaylistSongCrossRef)

    /**
     * Removes a song from a playlist.
     */
    @Delete
    suspend fun removeSongFromPlaylist(join: PlaylistSongCrossRef)

}