package com.colux.libretune.desktop.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.SearchSuggestion
import com.colux.libretune.data.repository.SearchRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed interface SearchUiState {
    data object Explore : SearchUiState
    data class Suggestions(val suggestions: List<SearchSuggestion>) : SearchUiState
    data class Results(val results: SearchResult) : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data object Loading : SearchUiState
}

class SearchViewModel(
    private val repository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Explore)
    val uiState = _uiState.asStateFlow()

    val moodGenres = repository.getMoodGenres()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        try { searchJob?.cancel() } catch (_: Exception) {}
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Explore
            return
        }
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

    fun submitSearch(query: String) {
        if (query.isBlank()) return
        try { searchJob?.cancel() } catch (_: Exception) {}
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            try {
                val result = repository.searchContent(query)
                _uiState.value = if (result == null || result.isEmpty) {
                    SearchUiState.Empty(query)
                } else {
                    SearchUiState.Results(result)
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Empty(query)
            }
        }
    }
}
