package com.colux.libretune.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.colux.libretune.data.model.Artist
import com.colux.libretune.data.model.HomeFeedItem
import com.colux.libretune.data.model.Playlist
import com.colux.libretune.data.model.Song
import com.colux.libretune.desktop.viewmodel.HomeUiState
import com.colux.libretune.desktop.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel, onSongClick: (Song) -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        HomeUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        HomeUiState.Empty -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Your home feed is empty.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Start by searching for music and saving artists or albums to your library.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }

        is HomeUiState.Success -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(state.items) { feedItem ->
                    when (feedItem) {
                        is HomeFeedItem.RelatedArtistsCarousel ->
                            ArtistCarouselSection(feedItem)

                        is HomeFeedItem.RelatedPlaylistsCarousel ->
                            PlaylistCarouselSection(feedItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistCarouselSection(item: HomeFeedItem.RelatedArtistsCarousel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Because you like ${item.artist.name}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(item.artists) { artist ->
                ArtistCard(artist)
            }
        }
    }
}

@Composable
private fun PlaylistCarouselSection(item: HomeFeedItem.RelatedPlaylistsCarousel) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Because you saved ${item.album.name}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(item.playlists) { playlist ->
                PlaylistCard(playlist)
            }
        }
    }
}

@Composable
private fun ArtistCard(artist: Artist) {
    Card(modifier = Modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = artist.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlaylistCard(playlist: Playlist) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (playlist.artists.isNotEmpty()) {
                Text(
                    text = playlist.getArtistNames(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
