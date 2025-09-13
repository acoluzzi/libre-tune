package com.colux.libretune.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.colux.libretune.data.model.HistoryItem
import com.colux.libretune.data.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class HistoryViewModel @Inject constructor(historyRepository: HistoryRepository) : ViewModel() {
    val historyItems: Flow<PagingData<HistoryListItem>> = Pager(
        config = PagingConfig(
            pageSize = 20, // How many items to load at once
            enablePlaceholders = false
        ),
        pagingSourceFactory = { historyRepository }
    ).flow
        .map { pagingData: PagingData<HistoryItem> ->
            pagingData.map { song ->
                HistoryListItem.SongItem(song)
            }
        }
        .map { pagingData ->
            pagingData.insertSeparators { before: HistoryListItem.SongItem?, after: HistoryListItem.SongItem? ->
                if (after == null) {
                    return@insertSeparators null
                }

                val afterDate = after.songPlayed.playedAt.toLocalDate()

                if (before == null) {
                    return@insertSeparators HistoryListItem.DateHeader(afterDate)
                }

                val beforeDate = before.songPlayed.playedAt.toLocalDate()
                if (beforeDate != afterDate) {
                    HistoryListItem.DateHeader(afterDate)
                } else {
                    null
                }
            }
        }
        .cachedIn(viewModelScope)
}

// Helper extension function to convert timestamp to LocalDate
@OptIn(ExperimentalTime::class)
private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}