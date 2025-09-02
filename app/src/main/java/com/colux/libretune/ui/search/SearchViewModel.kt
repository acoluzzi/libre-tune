package com.colux.libretune.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.SearchSuggestion
import com.colux.libretune.data.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Represents the different states the search screen can be in
sealed interface SearchUiState {
    data object Explore : SearchUiState
    data class Suggestions(val suggestions: List<SearchSuggestion>) : SearchUiState
    data class Results(val results: SearchResult) : SearchUiState
    data class Empty(val query: String) : SearchUiState
    data object Loading : SearchUiState
}


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Explore)
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    // This function is called on every keystroke from the UI
    fun onQueryChange(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Explore
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            repository.getQuerySuggestions(query).collect { suggestions ->
                _uiState.value = SearchUiState.Suggestions(suggestions)
            }
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