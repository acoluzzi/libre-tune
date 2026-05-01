package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.colux.libretune.data.local.entity.ArtistEntity
import com.colux.libretune.data.local.entity.LibraryEntity
import com.colux.libretune.data.local.entity.LibraryItemType
import com.colux.libretune.data.local.wrapper.LibraryItemWithArtistOrPlaylist
import com.colux.libretune.data.local.wrapper.PlaylistWithArtists
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {


    @Transaction
    @Query(
        """
        SELECT * FROM library
        ORDER BY addedAtTimestamp DESC
    """
    )
    fun getLibraryItems(): Flow<List<LibraryItemWithArtistOrPlaylist>>


    @Query(
        """
        SELECT artists.* FROM artists
        INNER JOIN library ON artists.artistId = library.artistId
        WHERE library.type = 'ARTIST'
        ORDER BY library.addedAtTimestamp DESC
        LIMIT :limit
    """
    )
    fun getSavedArtists(limit: Int): Flow<List<ArtistEntity>>

    @Transaction
    @Query(
        """
        SELECT playlists.* FROM playlists
        INNER JOIN library ON playlists.playlistId = library.playlistId
        WHERE library.type = 'PLAYLIST'
        ORDER BY library.addedAtTimestamp DESC
        LIMIT :limit
    """
    )
    fun getSavedPlaylists(limit: Int): Flow<List<PlaylistWithArtists>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(library: LibraryEntity)

    @Delete
    suspend fun delete(library: LibraryEntity)

    @Query(
        """
         SELECT EXISTS(
            SELECT 1 FROM library
            WHERE id = :libraryId AND type = :type
            LIMIT 1
        )
    """
    )
    fun isItemInLibrary(libraryId: String, type: LibraryItemType): Flow<Boolean>

    @Query(
        """
        DELETE FROM library
        WHERE type = 'PLAYLIST' AND playlistId IN (
            SELECT playlistId FROM playlists
            WHERE type IN ('ALBUM', 'SINGLE', 'EP')
        )
        """
    )
    suspend fun clearSavedAlbumLinks()

    @Query("DELETE FROM library WHERE type = 'ARTIST'")
    suspend fun clearSavedArtistLinks()
}