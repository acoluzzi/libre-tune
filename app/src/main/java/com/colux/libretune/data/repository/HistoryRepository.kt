package com.colux.libretune.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.colux.libretune.data.local.AppDatabase
import com.colux.libretune.data.local.mapper.toDataModel
import com.colux.libretune.data.model.HistoryItem
import javax.inject.Singleton

@Singleton
class HistoryRepository(
    private val db: AppDatabase
) : PagingSource<Int, HistoryItem>() {

    // This is the main function for loading data.
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HistoryItem> {
        return try {
            // The 'key' is the offset. For the first load, it will be null.
            val offset = params.key ?: 0

            // Fetch the data from our Room database.
            val items = db.historyDao().getHistory(
                limit = params.loadSize,
                offset = offset
            ).map {
                it.toDataModel()
            }

            // Return the data as a successful 'Page'.
            LoadResult.Page(
                data = items,
                // Key for the PREVIOUS page. Null if we're on the first page.
                prevKey = if (offset == 0) null else offset - params.loadSize,
                // Key for the NEXT page. Null if we've reached the end of the data.
                nextKey = if (items.isEmpty()) null else offset + items.size
            )

        } catch (e: Exception) {
            // Handle any errors by returning an Error result.
            LoadResult.Error(e)
        }
    }

    // This function helps Paging 3 figure out which page to load
    // when the data is refreshed.
    override fun getRefreshKey(state: PagingState<Int, HistoryItem>): Int? {
        // Try to find the page key of the closest page to the
        // last accessed index.
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}