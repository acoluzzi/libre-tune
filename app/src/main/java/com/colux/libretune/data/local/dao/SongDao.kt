package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.SongArtistCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkSongToArtists(crossRefs: List<SongArtistCrossRef>)

    // A query to get all songs for a given artist using the join table.
    @Query(
        """
        SELECT * FROM songs 
        WHERE songId IN (
            SELECT songId FROM song_artist_cross_ref WHERE artistId = :artistId
        )
    """
    )
    fun getSongsForArtist(artistId: String): Flow<List<SongEntity>>
}