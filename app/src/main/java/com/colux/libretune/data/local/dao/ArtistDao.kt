package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.join.ArtistArtistCrossRef
import kotlinx.coroutines.flow.Flow
import java.util.logging.Logger

@Dao
interface ArtistDao {
    private val logger: Logger
        get() = Logger.getLogger("ArtistDao")

    /**
     * Inserts or updates a single artist, but only if the new data is fresher.
     */
    @Transaction
    suspend fun upsert(artist: ArtistEntity) {
        val existingArtist = getArtistById(artist.artistId)
        if (existingArtist == null || (artist.updateTimestamp
                ?: 0L) >= (existingArtist.updateTimestamp ?: 0L)
        ) {
            _insert(artist)
        }
    }

    /**
     * Inserts or updates a list of artists, only saving the ones that are new or fresher.
     * This is more efficient than calling upsert for each item in a loop.
     */
    @Transaction
    suspend fun upsertAll(artists: List<ArtistEntity>) {
        val artistIds = artists.map { it.artistId }
        val existingArtists = getArtistsByIds(artistIds)
        val existingMap = existingArtists.associateBy { it.artistId }

        val artistsToInsert = artists.filter { newArtist ->
            val existing = existingMap[newArtist.artistId]
            val isToInsert =
                existing == null || (newArtist.updateTimestamp ?: 0L) >= (existing.updateTimestamp
                    ?: 0L)
            logger.info("Upsert album ${newArtist.artistId}: $isToInsert, existing=${existing?.updateTimestamp}, new=${newArtist.updateTimestamp}")
            isToInsert
        }

        if (artistsToInsert.isNotEmpty()) {
            logger.info("Inserting ${artistsToInsert.size} artists")
            _insertAll(artistsToInsert)
        }
    }

    // --- Internal Helpers ---
    // These are the raw insert methods, called by the upsert logic.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insert(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun _insertAll(artists: List<ArtistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun linkSimilarArtists(artistLinks: List<ArtistArtistCrossRef>)

    // A simple query that just returns the artist.
    @Query("SELECT * FROM artists WHERE artistId = :artistId")
    fun getArtist(artistId: String): Flow<ArtistEntity?>


    @Query(
        """
        SELECT * FROM artists 
        WHERE artistId IN (
            SELECT artistId FROM album_artist_cross_ref WHERE albumId = :albumId
        )
    """
    )
    fun getArtistsByAlbumId(albumId: String): Flow<List<ArtistEntity>>

    // A helper to get multiple artists at once for the upsertAll logic
    @Query("SELECT * FROM artists WHERE artistId IN (:artistIds)")
    suspend fun getArtistsByIds(artistIds: List<String>): List<ArtistEntity>


    // A suspend version for internal use
    @Query("SELECT * FROM artists WHERE artistId = :artistId")
    suspend fun getArtistById(artistId: String): ArtistEntity?

    @Query(
        """
        SELECT * FROM artists 
        WHERE artistId IN (
            SELECT relatedArtistId FROM artist_artist_cross_ref WHERE parentArtistId = :artistId
        )
    """
    )
    fun getSimilarArtists(artistId: String): Flow<List<ArtistEntity>>


}