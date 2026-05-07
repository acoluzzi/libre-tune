package com.colux.libretune.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.SearchSuggestion
import com.colux.libretune.data.repository.SearchRepository
import com.colux.libretune.ui.util.smartThrottle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

sealed interface SearchUiState {
    data object Explore : SearchUiState
    data class Suggestions(val suggestions: List<SearchSuggestion>) : SearchUiState
    data class Results(val results: SearchResult) : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data object Loading : SearchUiState
}


class SearchViewModel constructor(
    private val repository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Explore)
    val uiState = _uiState.asStateFlow()

    val moodGenres = repository.getMoodGenres().smartThrottle(30.minutes) { previous, _ ->
        previous == null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repository.getQuerySuggestions(query).collect { suggestions ->
                _uiState.value = SearchUiState.Suggestions(suggestions)
            }
        }
    }

    fun onFocusChanged(isFocused: Boolean, currentQuery: String) {
        if (isFocused) {
            onQueryChange(currentQuery)
        } else if (currentQuery.isBlank()) {
            _uiState.value = SearchUiState.Explore
        }
    }


    // This is called when the user submits the search
    fun submitSearch(query: String) {
        if (query.isBlank()) return
        searchJob?.cancel()

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            val result = repository.searchContent(query)
            if (result == null || result.isEmpty) {
                _uiState.value = SearchUiState.Empty(query)
            } else {
                _uiState.value = SearchUiState.Results(result)
            }
        }
    }

}