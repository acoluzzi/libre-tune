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

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, HistoryItem> {
        return try {
            val offset = params.key ?: 0

            val items = db.historyDao().getHistory(
                limit = params.loadSize,
                offset = offset
            ).map {
                it.toDataModel()
            }

            LoadResult.Page(
                data = items,
                prevKey = if (offset == 0) null else offset - params.loadSize,
                nextKey = if (items.isEmpty()) null else offset + items.size
            )

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, HistoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}