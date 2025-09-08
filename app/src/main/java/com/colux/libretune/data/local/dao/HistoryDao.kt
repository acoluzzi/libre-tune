package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.colux.libretune.data.local.entity.PlaybackHistoryEntity

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertHistory(item: PlaybackHistoryEntity)

    // A query to get the history with song details
//    @Transaction
//    @Query(
//        """
//        SELECT * FROM songs
//        WHERE songId IN (SELECT songId FROM playback_history ORDER BY playedAtTimestamp DESC)
//    """
//    )
//    fun getHistory(): Flow<List<PlaybackHistoryEntity>>
}