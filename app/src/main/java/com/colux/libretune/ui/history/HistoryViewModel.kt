package com.colux.libretune.ui.history

import androidx.lifecycle.ViewModel
import com.colux.libretune.data.local.dao.HistoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(historyDao: HistoryDao) : ViewModel() {
    //  val playbackHistory = historyDao.getHistory()
    //    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}