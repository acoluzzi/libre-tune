package com.colux.libretune.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedSongDao {
    // By returning a Flow, our UI will automatically update when the data changes.
    @Query("SELECT * FROM liked_songs ORDER BY title ASC")
    fun getLikedSongs(): Flow<List<LikedSongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun likeSong(song: LikedSongEntity)

    @Delete
    suspend fun unlikeSong(song: LikedSongEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE id = :songId)")
    fun isLiked(songId: String): Flow<Boolean>
}