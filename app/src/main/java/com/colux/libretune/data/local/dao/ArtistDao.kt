package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.join.ArtistArtistCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

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