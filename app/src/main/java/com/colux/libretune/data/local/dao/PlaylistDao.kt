package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.entity.PlaylistEntity
import com.colux.libretune.data.local.join.ArtistFeaturedPlaylistCrossRef
import com.colux.libretune.data.local.join.ArtistPlaylistCrossRef
import com.colux.libretune.data.local.join.PlaylistArtistCrossRef
import com.colux.libretune.data.local.join.PlaylistRelatedCrossRef
import com.colux.libretune.data.local.join.PlaylistSongCrossRef
import com.colux.libretune.data.local.wrapper.PlaylistWithArtists
import com.colux.libretune.data.local.wrapper.PlaylistWithSongsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertAll(playlists: List<PlaylistEntity>)


    /**
     * Inserts or updates a single item, but only if the new data is fresher.
     */
    @Transaction
    suspend fun upsert(playlist: PlaylistEntity) {
        val existingPlaylist = getPlaylistById(playlist.playlistId)
        if (existingPlaylist == null || (playlist.updateTimestamp
                ?: 0L) >= (existingPlaylist.updateTimestamp ?: 0L)
        ) {
            insert(playlist)
        }
    }

    /**
     * Inserts or updates a list of items, only saving the ones that are new or fresher.
     * This is more efficient than calling upsert for each item in a loop.
     */
    @Transaction
    suspend fun upsertAll(playlists: List<PlaylistEntity>) {
        val playlistIds = playlists.map { it.playlistId }
        val existingPlaylists = getPlaylistsByIds(playlistIds)
        val existingMap = existingPlaylists.associateBy { it.playlistId }

        val playlistsToInsert = playlists.filter { newPlaylist ->
            val existing = existingMap[newPlaylist.playlistId]
            existing == null || (newPlaylist.updateTimestamp ?: 0L) >= (existing.updateTimestamp
                ?: 0L)
        }

        if (playlistsToInsert.isNotEmpty()) {
            _insertAll(playlistsToInsert)
        }
    }

    @Query("SELECT * FROM playlists WHERE playlistId = :id")
    fun getPlaylist(id: String): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE playlistId = :id")
    fun getPlaylistWithArtists(id: String): Flow<PlaylistWithArtists?>


    @Query("SELECT * FROM playlists where isLocal = 1")
    fun getLocalPlaylists(): Flow<List<PlaylistWithSongsEntity>>

    @Query(
        """
        SELECT * 
        FROM playlists p 
        WHERE p.playlistId IN (
            SELECT DISTINCT playlistId 
            FROM library 
            WHERE type = 'PLAYLIST'
        ) OR p.isLocal = 1
    """
    )
    fun getSavedPlaylists(): Flow<List<PlaylistWithSongsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkAlbumToArtists(crossRefs: List<PlaylistArtistCrossRef>)

    @Query("SELECT * FROM playlists WHERE playlistId = :id")
    suspend fun getPlaylistById(id: String): PlaylistEntity?

    @Query("SELECT * FROM playlists WHERE playlistId IN (:playlistIds)")
    suspend fun getPlaylistsByIds(playlistIds: List<String>): List<PlaylistEntity>


    @Query("SELECT EXISTS(SELECT 1 FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId)")
    fun isSongInPlaylist(playlistId: String, songId: String): Flow<Boolean>

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 
            FROM playlist_song_cross_ref pscr join playlists p ON pscr.playlistId = p.playlistId
            WHERE pscr.playlistId = :playlistId AND pscr.songId = :songId AND p.isLocal = 1)
        """
    )
    fun isSongInLocalPlaylist(playlistId: String, songId: String): Flow<Boolean>


    @Query(
        """
        SELECT * FROM playlists
        WHERE playlistId IN (
            SELECT playlistId FROM artist_featured_playlist_cross_ref WHERE artistId = :artistId
        ) 
    """
    )
    fun getFeaturingPlaylistsForArtist(artistId: String): Flow<List<PlaylistEntity>>


    @Query(
        """
        SELECT * FROM playlists
        WHERE type = 'PLAYLIST'
        AND playlistId IN (
            SELECT playlistId FROM artist_playlists_cross_ref WHERE artistId = :artistId
        ) 
    """
    )
    fun getPlaylistsForArtist(artistId: String): Flow<List<PlaylistEntity>>


    @Query(
        """
        SELECT * FROM playlists
        WHERE (type = 'ALBUM' OR type = 'SINGLE' OR type = 'EP')
        AND playlistId IN (
            SELECT playlistId FROM playlist_artist_cross_ref WHERE artistId = :artistId
        ) 
    """
    )
    fun getAlbumsAndSinglesByArtistId(artistId: String): Flow<List<PlaylistEntity>>

    @Query(
        """
        SELECT * FROM playlists
        WHERE type = 'ALBUM' 
        AND playlistId IN (
            SELECT playlistId FROM playlist_artist_cross_ref WHERE artistId = :artistId
        ) 
    """
    )
    fun getAlbumsByArtistId(artistId: String): Flow<List<PlaylistEntity>>

    @Query(
        """
        SELECT * FROM playlists         
        WHERE (type = 'SINGLE' OR type = 'EP')
        AND playlistId IN (
            SELECT playlistId FROM playlist_artist_cross_ref WHERE artistId = :artistId
        ) 
    """
    )
    fun getSinglesByArtistId(artistId: String): Flow<List<PlaylistEntity>>


    @Query(
        """
        SELECT * FROM playlists
        WHERE playlistId IN (
            SELECT playlistId FROM playlist_related_cross_ref WHERE parentPlaylistId = :playlistId
        ) 
    """
    )
    fun getRelatedPlaylistsForPlaylist(playlistId: String): Flow<List<PlaylistEntity>>

    @Query(
        """
        SELECT * FROM playlists p
        WHERE p.playlistId IN (
            SELECT prcr.relatedPlaylistId 
            FROM playlist_related_cross_ref prcr 
            WHERE prcr.parentPlaylistId = :id
        )
        LIMIT :limit
    """
    )
    fun getRelatedPlaylistsWithArtists(id: String, limit: Int): Flow<List<PlaylistWithArtists>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkPlaylistToArtists(crossRefs: List<ArtistPlaylistCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkFeaturingPlaylistToArtists(crossRefs: List<ArtistFeaturedPlaylistCrossRef>)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkPlaylistsToRelatedPlaylists(crossRefs: List<PlaylistRelatedCrossRef>)

    /**
     * Adds a song to a playlist by inserting an entry into the join table.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSongToPlaylist(join: PlaylistSongCrossRef)

    @Query(
        """
       DELETE FROM playlist_song_cross_ref 
          WHERE songId = :songId 
            AND playlistId IN (SELECT playlistId FROM playlists WHERE isLocal = 1)
    """
    )
    suspend fun removeSongFromAllLocalPlaylists(songId: String)


    /**
     * Removes a song from a playlist.
     */
    @Delete
    suspend fun removeSongFromPlaylist(join: PlaylistSongCrossRef)


}