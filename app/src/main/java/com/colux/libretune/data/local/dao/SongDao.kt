package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colux.libretune.data.local.entity.SongEntity

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)


    @Query("SELECT * FROM songs WHERE albumId = :albumId")
    suspend fun getSongsByAlbumId(albumId: String): List<SongEntity>
}