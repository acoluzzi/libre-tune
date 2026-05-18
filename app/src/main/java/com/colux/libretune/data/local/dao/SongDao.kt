package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.entity.SongEntity
import com.colux.libretune.data.local.join.SongArtistCrossRef
import com.colux.libretune.data.local.wrapper.SongWithAlbumAndArtist
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(songs: SongEntity)


    @Query("SELECT * FROM songs WHERE albumId = :albumId")
    suspend fun getSongsByAlbumId(albumId: String): List<SongEntity>

    @Transaction
    @Query(
        """
        
        SELECT * 
        FROM songs 
        WHERE songId IN (
            SELECT songId 
            FROM song_artist_cross_ref 
            WHERE artistId = :artistId
        )
        
        """
    )
    fun getSongsWithAlbumByArtistId(artistId: String): Flow<List<SongWithAlbumAndArtist>>

    @Transaction
    @Query(
        """
        
        SELECT * 
        FROM songs 
        WHERE songId IN (
            SELECT songId 
            FROM song_artist_cross_ref 
            WHERE artistId = :artistId AND isTopSong = 1
        )
        
        """
    )
    fun getTopSongsWithAlbumByArtistId(artistId: String): Flow<List<SongWithAlbumAndArtist>>

    @Transaction
    @Query("SELECT * FROM songs WHERE albumId = :albumId")
    fun getSongsWithAlbumAndArtistByAlbumId(albumId: String): Flow<List<SongWithAlbumAndArtist>>

    @Transaction
    @Query("SELECT * FROM songs WHERE songId IN (SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId)")
    fun getSongsWithAlbumAndArtistByPlaylistId(playlistId: String): Flow<List<SongWithAlbumAndArtist>>


    @Query("SELECT * FROM songs WHERE songId = :id")
    fun getSongById(id: String): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE songId = :id")
    suspend fun getSongByIdOnce(id: String): SongEntity?


    @Query(
        """
        SELECT * FROM songs 
        WHERE songId IN (
            SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId
        )
    """
    )
    suspend fun getSongsInPlaylist(playlistId: String): List<SongEntity>


    @Query(
        """
        SELECT DISTINCT songId 
        FROM playlist_song_cross_ref 
        WHERE playlistId IN (
            SELECT playlistId 
            FROM playlists 
            WHERE isLocal = 1 AND type = 'PLAYLIST'
        )
        """
    )
    fun getSavedPlaylistSongIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkSongToArtist(crossRef: SongArtistCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun linkSongsToArtists(crossRefs: List<SongArtistCrossRef>)
}