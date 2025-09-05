package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colux.libretune.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(songs: SongEntity)


    @Query("SELECT * FROM songs WHERE albumId = :albumId")
    suspend fun getSongsByAlbumId(albumId: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE songId = :id")
    fun getSongById(id: String): Flow<SongEntity?>


    @Query(
        """
        SELECT * FROM songs 
        WHERE songId IN (
            SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId
        )
    """
    )
    suspend fun getSongsInPlaylist(playlistId: String): List<SongEntity>
}