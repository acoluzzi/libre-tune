package com.colux.libretune.data.repository

import com.colux.libretune.data.local.dao.SearchQueryDao
import com.colux.libretune.data.local.entity.SearchQueryEntity
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.SearchSuggestion
import com.colux.libretune.data.remote.tube.YouTubeExtractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val remote: YouTubeExtractionRepository,
    private val searchQueryDao: SearchQueryDao,
) {

    suspend fun searchContent(query: String): SearchResult? {
        val result = remote.searchContent(query)
        if (result != null && !result.isEmpty) {
            searchQueryDao.insertQuery(SearchQueryEntity(query = query))
        }
        return result
    }

    fun getQuerySuggestions(query: String): Flow<List<SearchSuggestion>> {
        val fromHistory: Flow<List<SearchSuggestion>> =
            searchQueryDao.getQuerySuggestions(query).map {
                it.map { entity -> SearchSuggestion.QuerySuggestion(entity.query, true) }
            }

        val fromRemote: Flow<List<SearchSuggestion>> = flow {
            val remoteResults = remote.searchSuggestions(query)
            emit(remoteResults)
        }

        return combine(fromHistory, fromRemote) { history, remote ->
            (history + remote)
        }
    }
}