package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.PlaybackHistoryEntity
import com.colux.libretune.data.local.join.HistoryArtistCrossRef
import com.colux.libretune.data.local.wrapper.HistoryItemWithSongAlbumAndFirstArtist
import com.colux.libretune.data.local.wrapper.PlaylistWithArtists
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insertHistory(item: PlaybackHistoryEntity)


    @Query("SELECT last_insert_rowid()")
    suspend fun getLastInsertedId(): Long

    @Insert
    suspend fun linkHistoryItemToArtists(crossRef: List<HistoryArtistCrossRef>)

    @Query(
        """
        SELECT *
        FROM playback_history 
        GROUP BY songId 
        ORDER BY MAX(playedAtTimestamp) DESC
        LIMIT :limit 
        OFFSET :offset
        """
    )
    suspend fun getHistory(limit: Int, offset: Int): List<HistoryItemWithSongAlbumAndFirstArtist>

    // Gets the most recently played, unique artists
    @Query(
        """
        SELECT artists.* FROM artists 
        WHERE artistId IN (
            SELECT har.artistId 
            FROM playback_history ph 
            JOIN history_artist_cross_ref har ON ph.historyId = har.historyId
            GROUP BY har.artistId 
            ORDER BY MAX(ph.playedAtTimestamp) DESC
        )
        LIMIT :limit
    """
    )
    fun getRecentlyPlayedArtists(limit: Int): Flow<List<ArtistEntity>>

    // Gets the most recently played, unique albums
    @Query(
        """
        SELECT playlists.* FROM playlists
        WHERE playlistId IN (
            SELECT albumId FROM playback_history
            GROUP BY albumId
            ORDER BY MAX(playedAtTimestamp) DESC
        )
        LIMIT :limit
    """
    )
    fun getRecentlyPlayedAlbums(limit: Int): Flow<List<PlaylistWithArtists>>


}