package com.colux.libretune.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- DATA MODELS FOR THE NEW UI STATE ---

// Represents the different states the search screen can be in
sealed interface SearchUiState {
    data object Explore : SearchUiState
    data class Suggestions(val suggestions: List<String>) : SearchUiState
    data class Results(val results: CategorizedSearchResults) : SearchUiState
    data object Loading : SearchUiState
}

// Holds the categorized search results
data class CategorizedSearchResults(
    val topResults: List<Any>, // A mix of Song, Artist, etc.
    val songs: List<Song>,
    val artists: List<Artist>,
    val albums: List<Playlist>,
    val featuredPlaylists: List<Playlist>,
    val communityPlaylists: List<Playlist>
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    // private val repository: YourRepository,
    // private val searchHistoryDao: SearchHistoryDao
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

        // Debounce the search to avoid too many API calls
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Wait for 300ms of no typing
            // In a real app, you would fetch suggestions from the repository
            val mockSuggestions = listOf("$query suggestion 1", "$query suggestion 2", "Past query")
            _uiState.value = SearchUiState.Suggestions(mockSuggestions)
        }
    }

    // This is called when the user submits the search
    fun submitSearch(query: String) {
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading
            delay(1000) // Simulate network call

            // In a real app, you'd get this from the repository
            val mockResults = CategorizedSearchResults(
                topResults = listOf(),
                songs = DummyData.songs,
                artists = DummyData.artists,
                albums = DummyData.playlists,
                featuredPlaylists = DummyData.playlists,
                communityPlaylists = DummyData.playlists
            )
            _uiState.value = SearchUiState.Results(mockResults)
        }
    }
}