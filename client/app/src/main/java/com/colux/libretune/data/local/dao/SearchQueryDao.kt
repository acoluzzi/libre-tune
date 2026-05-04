package com.colux.libretune.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.colux.libretune.data.local.entity.SearchQueryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchQueryDao {

    /**
     * Inserts a new search query. If the query already exists, it does nothing.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuery(query: SearchQueryEntity)

    /**
     * Gets the most recent queries that match the user's typing.
     * For example, if the user types "que", it could return "queen".
     */
    @Query("SELECT * FROM search_history WHERE `query` LIKE :query || '%' ORDER BY timestamp DESC LIMIT 5")
    fun getQuerySuggestions(query: String): Flow<List<SearchQueryEntity>>

    /**
     * Gets the most recent queries when the search box is empty.
     */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 5")
    fun getRecentQueries(): Flow<List<SearchQueryEntity>>
}