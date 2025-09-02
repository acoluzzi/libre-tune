package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import kotlinx.coroutines.flow.Flow


@Dao
interface AlbumDao {
    // --- Basic Insertion Methods ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkAlbumToArtists(crossRefs: List<AlbumArtistCrossRef>)


    @Transaction
    @Query("SELECT * FROM albums WHERE albumId = :albumId")
    fun getAlbum(albumId: String): Flow<AlbumEntity?>


    @Query(
        """
        SELECT * FROM albums
        WHERE type = 'ALBUM' 
        AND albumId IN (
            SELECT albumId FROM album_artist_cross_ref WHERE artistId = :artistId
        ) 
    """
    )
    fun getAlbumsByArtistId(artistId: String): Flow<List<AlbumEntity>>

    @Query(
        """
        SELECT * FROM albums         
        WHERE type = 'SINGLE_EP' 
        AND albumId IN (
            SELECT albumId FROM album_artist_cross_ref WHERE artistId = :artistId
        ) 
    """
    )
    fun getSinglesByArtistId(artistId: String): Flow<List<AlbumEntity>>


}