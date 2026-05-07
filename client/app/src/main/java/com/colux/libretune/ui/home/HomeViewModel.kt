package com.colux.libretune.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.HomeFeedItem
import com.colux.libretune.data.repository.HomeRepository
import com.colux.libretune.ui.util.smartThrottle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.minutes

class HomeViewModel constructor(
    private val homeRepository: HomeRepository
) :
    ViewModel() {

    private val logger = java.util.logging.Logger.getLogger(HomeViewModel::class.java.name)

    val uiState: StateFlow<HomeUiState> = fetchState()
        .smartThrottle(30.minutes) { previous, current ->
            previous == null ||
                    previous !is HomeUiState.Success
                    || previous.items.isEmpty()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = HomeUiState.Loading
        )


    fun fetchState(): Flow<HomeUiState> {
        val historySuggestions = homeRepository.getHomeScreenFeed()
        return historySuggestions
            .map { suggestions ->
                if (suggestions.isEmpty()) {
                    logger.info { "Empty suggestions" }
                    HomeUiState.Empty
                } else {

                    logger.info { "Success suggestions" }
                    HomeUiState.Success(suggestions)
                }
            }
    }


}


sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(val items: List<HomeFeedItem>) : HomeUiState
}