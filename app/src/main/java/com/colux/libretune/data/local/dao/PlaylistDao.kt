package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.relation.PlaylistWithSongs
import kotlinx.coroutines.flow.Flow

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

    /**
     * Fetches a single playlist with its complete list of songs.
     * @Transaction is crucial here. It ensures that Room runs the two
     * separate queries (one for the playlist and one for its songs) together
     * as a single, atomic operation to prevent data inconsistency.
     */
    @Transaction
    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    fun getPlaylistWithSongs(playlistId: String): Flow<PlaylistWithSongs>

    /**
     * Fetches all playlists with their complete list of songs.
     */
    @Transaction
    @Query("SELECT * FROM playlists")
    fun getAllPlaylistsWithSongs(): Flow<List<PlaylistWithSongs>>
}