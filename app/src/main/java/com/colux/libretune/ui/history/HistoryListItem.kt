package com.colux.libretune.ui.history

import com.colux.libretune.data.model.HistoryItem
import java.time.LocalDate

sealed interface HistoryListItem {
    data class SongItem(val songPlayed: HistoryItem) : HistoryListItem
    data class DateHeader(val date: LocalDate) : HistoryListItem
}