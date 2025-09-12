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
            // First, map the raw Song objects to our sealed interface type
            pagingData.map { song ->
                HistoryListItem.SongItem(song)
            }
        }
        .map { pagingData ->
            // Now, insert the date separators
            pagingData.insertSeparators { before: HistoryListItem.SongItem?, after: HistoryListItem.SongItem? ->
                // Logic to decide if a separator is needed
                if (after == null) {
                    // End of the list, no separator needed
                    return@insertSeparators null
                }

                val afterDate = after.songPlayed.playedAt.toLocalDate()

                if (before == null) {
                    // This is the very first item, so it needs a header
                    return@insertSeparators HistoryListItem.DateHeader(afterDate)
                }

                // Check if the day is different between the 'before' and 'after' items
                val beforeDate = before.songPlayed.playedAt.toLocalDate()
                if (beforeDate != afterDate) {
                    HistoryListItem.DateHeader(afterDate)
                } else {
                    // Same day, no separator needed
                    null
                }
            }
        }
        // Cache the results in the viewModelScope to survive configuration changes
        .cachedIn(viewModelScope)
}

// Helper extension function to convert timestamp to LocalDate
@OptIn(ExperimentalTime::class)
private fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}