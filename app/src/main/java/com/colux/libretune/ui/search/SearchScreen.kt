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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.Image
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.SearchSuggestion
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.components.album.PlaylistItem
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.components.song.SongMenu
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel

// --- DATA MODELS (as provided by you, with fixes) ---

// A simple object to hold updated mock data for the previews
object DummyData {
    private val imageList = listOf(Image("https://picsum.photos/seed/music/200", 200, 200))
    private val queen = Artist("a1", "Queen", imageList)
    private val ledZeppelin = Artist("a2", "Led Zeppelin", imageList)

    val songs = listOf(
        Song("s1", "Bohemian Rhapsody", listOf(queen), null, 12313123, imageList),
        Song("s2", "Stairway to Heaven", listOf(ledZeppelin), null, 123123123, imageList)
    )
    val artists = listOf(queen, ledZeppelin)
    val playlists = listOf(
        Playlist("p1", "Rock Classics", imageList, listOf(queen, ledZeppelin), isLocal = false),
        Playlist("p2", "70s Hits", imageList, listOf(ledZeppelin), isLocal = false)
    )
    val genres = listOf(
        "Rock", "Pop", "Hip Hop", "Jazz", "Classical", "Electronic"
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(playerViewModel: PlayerViewModel, navController: NavHostController) {
    val searchViewModel: SearchViewModel = hiltViewModel()
    val uiState by searchViewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // --- State for the Bottom Sheet ---
    val sheetState = rememberModalBottomSheetState()
    // This holds the song that the user tapped the menu for. If null, the sheet is hidden.
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
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
                    searchViewModel.onFocusChanged(focusState.isFocused, query)
                },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                searchViewModel.submitSearch(query)
                focusManager.clearFocus()
            }),

            // 1. Add the leading search icon
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search Icon")
            },

            // 2. Add the clearable trailing icon
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        searchViewModel.onQueryChange("")
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },

            // 3. Customize the colors
            colors = TextFieldDefaults.colors(
                // Use surfaceVariant for a "less black" background
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),

                // Use onSurfaceVariant for a "more gray" text color
                focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),

                // Customize other colors for a polished look
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),

                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )


        // --- Main Content Area ---
        Box(modifier = Modifier.fillMaxSize()) {
            // Show suggestions only when the user is typing and the text field is focused

            if (isFocused) {
                if (uiState is SearchUiState.Suggestions) {
                    SuggestionsOverlay(
                        suggestions = (uiState as SearchUiState.Suggestions).suggestions,
                        onSuggestionClick = { suggestion ->
                            when (suggestion) {
                                is SearchSuggestion.QuerySuggestion -> {
                                    // If it's a simple query, submit a new search
                                    query = suggestion.query
                                    searchViewModel.submitSearch(suggestion.query)
                                }

                                is SearchSuggestion.EntitySuggestion -> {
                                    // If it's a specific entity, handle based on type
                                    when (suggestion.type) {
                                        "Song" -> {
                                            playerViewModel.playPlaylist(
                                                listOfNotNull(suggestion.song),
                                                0
                                            )
                                        }

                                        "Artist" -> {
                                            navController.navigate(
                                                Screen.Artist.createRoute(
                                                    suggestion.artist?.id
                                                        ?: return@SuggestionsOverlay
                                                )
                                            )
                                        }

                                        "Album", "Playlist" -> {
                                            navController.navigate(
                                                Screen.PlaylistDetail.createRoute(
                                                    suggestion.album?.id ?: suggestion.playlist?.id
                                                    ?: return@SuggestionsOverlay
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            // Hide the keyboard and suggestions overlay
                            focusManager.clearFocus()
                        }
                    )
                }
            } else {
                // If not focused, we are in "browse" or "results" mode.
                when (val state = uiState) {
                    is SearchUiState.Explore -> ExplorePanel(genres = DummyData.genres)
                    is SearchUiState.Loading -> SearchResultsSkeleton()
                    is SearchUiState.Empty -> EmptyResults(query = state.query)
                    is SearchUiState.Results -> SearchResultsList(
                        results = state.results,
                        navController = navController,
                        playerViewModel = playerViewModel,
                        onSongMenuClick = { song -> selectedSongForMenu = song }
                    )
                    // Suggestions state is ignored when not focused
                    is SearchUiState.Suggestions -> ExplorePanel(genres = DummyData.genres)
                }

                if (selectedSongForMenu != null) {
                    ModalBottomSheet(
                        onDismissRequest = { selectedSongForMenu = null },
                        sheetState = sheetState
                    ) {
                        // We can reuse the menu from the full-screen player
                        SongMenu(
                            song = selectedSongForMenu!!,
                            onClose = {
                                selectedSongForMenu = null
                            },
                            navController = navController,
                            playerViewModel = playerViewModel,
                        )
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
fun EmptyResults(query: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No results found for \"$query\"",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun SuggestionsOverlay(
    suggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .clickable { /* Absorb clicks to not interact with content below */ }
            .background(MaterialTheme.colorScheme.surface)
    ) {
        items(suggestions) { suggestion ->
            // Use a 'when' statement to handle each type of suggestion
            when (suggestion) {
                is SearchSuggestion.QuerySuggestion -> {
                    QuerySuggestionItem(
                        suggestion = suggestion,
                        onClick = { onSuggestionClick(suggestion) }
                    )
                }

                is SearchSuggestion.EntitySuggestion -> {
                    EntitySuggestionItem(
                        suggestion = suggestion,
                        onClick = { onSuggestionClick(suggestion) }
                    )
                }
            }
        }
    }
}

/**
 * A composable for displaying a simple text suggestion (past or remote).
 */
@Composable
fun QuerySuggestionItem(suggestion: SearchSuggestion.QuerySuggestion, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Show History or Search icon based on the 'isFromHistory' flag
        val icon = if (suggestion.isFromHistory) Icons.Outlined.History else Icons.Outlined.Search
        Icon(
            imageVector = icon,
            contentDescription = if (suggestion.isFromHistory) "Past query" else "Search suggestion"
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = suggestion.query)
    }
}

/**
 * A composable for displaying an entity suggestion (Song, Artist, etc.).
 */
@Composable
fun EntitySuggestionItem(suggestion: SearchSuggestion.EntitySuggestion, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = suggestion.getImageForList(),
            contentDescription = suggestion.getName(),
            modifier = Modifier
                .size(48.dp)
                .clip(if (suggestion.type == "Artist") CircleShape else RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = suggestion.getName(), style = MaterialTheme.typography.bodyLarge)

            // Build a subtitle that includes the type and artist name if present.
            val subtitle = buildString {
                append(suggestion.type)
                if (!suggestion.getArtistNameLabel().isNullOrBlank()) {
                    append(" • ${suggestion.getArtistNameLabel()}")
                }
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SearchResultsList(
    results: SearchResult,
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    onSongMenuClick: (Song) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 64.dp)) {


        if (results.hasTopResults) {
            item {
                Text(
                    "Best Results",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            items(results.topArtists) { artist ->
                ArtistSearchResultItem(
                    artist = artist,
                    onClick = {
                        navController.navigate(Screen.Artist.createRoute(artist.id))
                    })
            }


            items(results.topAlbums) { album ->
                PlaylistItem(
                    album = album,
                    onClick = {
                        navController.navigate(Screen.PlaylistDetail.createRoute(album.id))
                    })
            }

            items(results.topSongs) { song ->
                SongItem(
                    song = song,
                    playerViewModel = playerViewModel,
                    onMoreClick = {
                        onSongMenuClick(song)
                    },
                    onClick = {
                        playerViewModel.playPlaylist(
                            results.topSongs,
                            results.topSongs.indexOf(song)
                        )
                    },
                    isInPlaylist = false,
                    navController = navController
                )
            }


        }


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
                SongItem(
                    song = song,
                    playerViewModel = playerViewModel,
                    onMoreClick = {
                        onSongMenuClick(song)
                    },
                    isInPlaylist = false,
                    navController = navController,
                    onClick = {
                        playerViewModel.playPlaylist(results.songs, results.songs.indexOf(song))
                    })
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
                    onClick = {
                        navController.navigate(Screen.Artist.createRoute(artist.id))
                    })
            }
        }

        if (results.albums.isNotEmpty()) {
            item {
                Text(
                    "Albums",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(results.albums) { album ->
                PlaylistItem(
                    album = album,
                    onClick = {
                        navController.navigate(Screen.PlaylistDetail.createRoute(album.id))
                    })
            }
        }

        if (results.playlists.isNotEmpty()) {
            item {
                Text(
                    "Featured Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(results.playlists) { playlist ->
                PlaylistItem(
                    album = playlist,
                    onClick = {
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                    })
            }
        }

        if (results.communityPlaylists.isNotEmpty()) {
            item {
                Text(
                    "Community Playlists",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(results.communityPlaylists) { playlist ->
                PlaylistItem(
                    album = playlist,
                    onClick = {
                        navController.navigate(Screen.PlaylistDetail.createRoute(playlist.id))
                    })
            }
        }
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
        Column {
            Text(text = artist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            Text(
                text = "Artist",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }


    }
}