package com.colux.libretune.ui.mood_genre

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.MoodGenreCategory
import com.colux.libretune.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MoodGenreViewModel @Inject constructor(
    searchRepository: SearchRepository, savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val moodGenreId: String = savedStateHandle.get<String>("moodGenreId")!!

    val uiState: StateFlow<GenreMoodUiState> =
        searchRepository.getMoodGenreCategory(moodGenreId)
            .map { details ->
                if (details != null) {
                    GenreMoodUiState.Success(details)
                } else {
                    GenreMoodUiState.Loading
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GenreMoodUiState.Loading
            )
}


sealed interface GenreMoodUiState {
    data object Loading : GenreMoodUiState
    data class Success(val details: MoodGenreCategory) : GenreMoodUiState
    data class Error(val message: String) : GenreMoodUiState
}