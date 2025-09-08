package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colux.libretune.data.local.entity.LibraryEntity
import com.colux.libretune.data.local.entity.LibraryItemType
import com.colux.libretune.data.local.wrapper.LibraryItemWithArtistOrPlaylist
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {


    @Query(
        """
        SELECT * FROM library
        ORDER BY addedAtTimestamp DESC
    """
    )
    fun getLibraryItems(): Flow<List<LibraryItemWithArtistOrPlaylist>>


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
}