package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.entity.AlbumEntity
import com.colux.libretune.data.local.join.AlbumArtistCrossRef
import kotlinx.coroutines.flow.Flow
import java.util.logging.Logger


@Dao
interface AlbumDao {

    private val logger: Logger
        get() = Logger.getLogger("AlbumDao")

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insert(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertAll(albums: List<AlbumEntity>)


    @Query("SELECT * FROM albums WHERE albumId = :id")
    fun getAlbum(id: String): Flow<AlbumEntity?>

    /**
     * Inserts or updates a single item, but only if the new data is fresher.
     */
    @Transaction
    suspend fun upsert(album: AlbumEntity) {
        val existingAlbum = getAlbumById(album.albumId)
        if (existingAlbum == null || (album.updateTimestamp
                ?: 0L) >= (existingAlbum.updateTimestamp ?: 0L)
        ) {
            _insert(album)
        }
    }

    /**
     * Inserts or updates a list of items, only saving the ones that are new or fresher.
     * This is more efficient than calling upsert for each item in a loop.
     */
    @Transaction
    suspend fun upsertAll(albums: List<AlbumEntity>) {
        val albumIds = albums.map { it.albumId }
        val existingAlbums = getAlbumsByIds(albumIds)
        val existingMap = existingAlbums.associateBy { it.albumId }

        val albumsToInsert = albums.filter { newAlbum ->
            val existing = existingMap[newAlbum.albumId]
            val isToInsert =
                existing == null || (newAlbum.updateTimestamp ?: 0L) >= (existing.updateTimestamp
                    ?: 0L)
            isToInsert
        }

        if (albumsToInsert.isNotEmpty()) {
            logger.info("Inserting ${albumsToInsert.size} albums")
            _insertAll(albumsToInsert)
        }
    }

    @Query("SELECT * FROM albums WHERE albumId = :id")
    suspend fun getAlbumById(id: String): AlbumEntity?

    @Query("SELECT * FROM albums WHERE albumId IN (:albumIds)")
    suspend fun getAlbumsByIds(albumIds: List<String>): List<AlbumEntity>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkAlbumToArtists(crossRefs: List<AlbumArtistCrossRef>)


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