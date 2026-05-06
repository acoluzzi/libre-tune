package com.colux.libretune.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.SearchResult
import com.colux.libretune.data.model.Song
import com.colux.libretune.desktop.viewmodel.SearchUiState
import com.colux.libretune.desktop.viewmodel.SearchViewModel

@Composable
fun SearchScreen(viewModel: SearchViewModel, onSongClick: (com.colux.libretune.data.model.Song) -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    fun submitQuery(q: String) {
        query = q
        viewModel.submitSearch(q)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.onQueryChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .onKeyEvent { event ->
                    if (event.key == Key.Enter && event.type == KeyEventType.KeyDown) {
                        submitQuery(query)
                        true
                    } else false
                },
            placeholder = { Text("Search for songs, artists, albums…") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { submitQuery(query) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submitQuery(query) }),
        )

        when (val state = uiState) {
            SearchUiState.Explore -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Type something to search…",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            SearchUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is SearchUiState.Empty -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No results for \"${state.query}\"",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            is SearchUiState.Suggestions -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                ) {
                    items(state.suggestions) { suggestion ->
                        Text(
                            text = suggestion.getName(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { submitQuery(suggestion.getName()) }
                                .padding(vertical = 12.dp),
                        )
                        HorizontalDivider()
                    }
                }
            }

            is SearchUiState.Results -> {
                SearchResultsView(state.results, onSongClick)
            }
        }
    }
}

@Composable
private fun SearchResultsView(result: SearchResult, onSongClick: (Song) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (result.topSongs.isNotEmpty()) {
            item {
                Text("Top Songs", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            items(result.topSongs) { SongRow(it, onSongClick) }
        }

        if (result.topArtists.isNotEmpty()) {
            item {
                Text("Artists", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            items(result.topArtists) { ArtistRow(it) }
        }

        if (result.topAlbums.isNotEmpty()) {
            item {
                Text("Albums", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            items(result.topAlbums) { AlbumRow(it) }
        }

        if (result.songs.isNotEmpty()) {
            item {
                Text("Songs", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            }
            items(result.songs) { SongRow(it, onSongClick) }
        }
    }
}

@Composable
private fun SongRow(song: Song, onSongClick: (Song) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onSongClick(song) }.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.getArtistNames(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    HorizontalDivider()
}

@Composable
private fun ArtistRow(artist: Artist) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(artist.name, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider()
}

@Composable
private fun AlbumRow(playlist: Playlist) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = buildString {
                    append(playlist.getArtistNames())
                    if (playlist.releaseYear != null) append(" • ${playlist.releaseYear}")
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = playlist.getTypeLabel(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
    HorizontalDivider()
}
