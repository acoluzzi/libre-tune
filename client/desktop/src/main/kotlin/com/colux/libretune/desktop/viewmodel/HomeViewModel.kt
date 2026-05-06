package com.colux.libretune.desktop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.HomeFeedItem
import com.colux.libretune.data.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.logging.Logger

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(val items: List<HomeFeedItem>) : HomeUiState
}

class HomeViewModel(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val logger = Logger.getLogger(HomeViewModel::class.java.name)

    val uiState: StateFlow<HomeUiState> = fetchState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )

    private fun fetchState(): Flow<HomeUiState> =
        homeRepository.getHomeScreenFeed().map { suggestions ->
            if (suggestions.isEmpty()) {
                logger.info("Home feed is empty")
                HomeUiState.Empty
            } else {
                logger.info("Home feed loaded (${suggestions.size} items)")
                HomeUiState.Success(suggestions)
            }
        }
}
