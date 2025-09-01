package com.colux.libretune.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.Image
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.player.PlayerViewModel

// --- DATA MODELS (as provided by you, with fixes) ---

// A simple object to hold updated mock data for the previews
object DummyData {
    private val imageList = listOf(Image("https://picsum.photos/seed/music/200", 200, 200))
    private val queen = Artist("a1", "Queen", imageList)
    private val ledZeppelin = Artist("a2", "Led Zeppelin", imageList)

    val songs = listOf(
        Song("s1", "Bohemian Rhapsody", listOf(queen), null, imageList),
        Song("s2", "Stairway to Heaven", listOf(ledZeppelin), null, imageList)
    )
    val artists = listOf(queen, ledZeppelin)
    val playlists = listOf(
        Playlist("p1", "Rock Classics", imageList, listOf(queen, ledZeppelin)),
        Playlist("p2", "70s Hits", imageList, listOf(ledZeppelin))
    )
    val genres = listOf(
        "Rock", "Pop", "Hip Hop", "Jazz", "Classical", "Electronic"
    )
}


@Composable
fun SearchScreen(playerViewModel: PlayerViewModel, navController: NavHostController) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    val uiState by searchViewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Search Input Field ---
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                searchViewModel.onQueryChange(it)
            },
            label = { Text("What do you want to play?") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                searchViewModel.submitSearch(query)
                focusManager.clearFocus() // Hide keyboard and unfocus
            }),
            // The clearable icon
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        searchViewModel.onQueryChange("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )

        // --- Main Content Area ---
        Box(modifier = Modifier.fillMaxSize()) {
            // Show suggestions only when the user is typing and the text field is focused
            if (isFocused && query.isNotEmpty() && uiState is SearchUiState.Suggestions) {
                SuggestionsOverlay(
                    suggestions = (uiState as SearchUiState.Suggestions).suggestions,
                    onSuggestionClick = { suggestion ->
                        query = suggestion
                        searchViewModel.submitSearch(suggestion)
                        focusManager.clearFocus()
                    }
                )
            } else {
                // Otherwise, show Explore, Loading, or Results based on the state
                when (val state = uiState) {
                    is SearchUiState.Explore -> ExplorePanel(genres = DummyData.genres)
                    is SearchUiState.Loading -> SearchResultsSkeleton()

                    is SearchUiState.Results -> SearchResultsList(results = state.results)
                    is SearchUiState.Suggestions -> {
                        // This case is handled by the `if` check above, but we can show
                        // the explore panel as a background for a better look.
                        ExplorePanel(genres = DummyData.genres)
                    }
                }
            }
        }
    }
}

// --- UI Components for Each State ---

@Composable
fun ExplorePanel(genres: List<String>) {
    Column {
        Text(
            text = "Browse all",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                Card(modifier = Modifier.height(100.dp)) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(genre)
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionsOverlay(suggestions: List<String>, onSuggestionClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .clickable { /* Absorb clicks to not interact with content below */ }
            .background(MaterialTheme.colorScheme.surface)
    ) {
        items(suggestions) { suggestion ->
            Text(
                text = suggestion,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuggestionClick(suggestion) }
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun SearchResultsList(results: CategorizedSearchResults) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // --- Songs Section ---
        if (results.songs.isNotEmpty()) {
            item {
                Text(
                    "Songs",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(results.songs) { song ->
                SongItem(song = song, onClick = { /* TODO: playerViewModel.play... */ })
            }
        }

        // --- Artists Section ---
        if (results.artists.isNotEmpty()) {
            item {
                Text(
                    "Artists",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(results.artists) { artist ->
                ArtistSearchResultItem(
                    artist = artist,
                    onClick = { /* TODO: navController.navigate... */ })
            }
        }

        // You would add sections for albums, playlists, etc. in the same way.
    }
}


@Composable
fun ArtistSearchResultItem(artist: Artist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artist.bestImageForCarousel(),
            contentDescription = artist.name,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape) // Artists often have circular profile images
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = artist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
    }
}