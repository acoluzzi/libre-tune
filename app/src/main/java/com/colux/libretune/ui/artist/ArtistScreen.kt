package com.colux.libretune.ui.artist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.colux.libretune.ui.components.album.SongsCarousel
import com.colux.libretune.ui.player.PlayerViewModel

@Composable
fun ArtistScreen(
    artistId: String,
    playerViewModel: PlayerViewModel,
    viewModel: ArtistDetailViewModel = hiltViewModel()
) {
    val artistDetails by viewModel.artistDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (artistDetails != null) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Artist Banner
            item {
                Box(modifier = Modifier.height(200.dp)) {
                    AsyncImage(
                        model = artistDetails?.bannerUrl,
                        contentDescription = "Artist Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = artistDetails!!.name,
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    )
                }
            }

            // Top Songs Carousel
            item {
                SongsCarousel(
                    title = "Top Songs",
                    songs = artistDetails!!.topSongs,
                    onSongClick = { index ->
                        playerViewModel.playSongById(artistDetails!!.topSongs[index].id)
                    }
                )
            }

            // Similar Artists Carousel
            item {
                // You'll need to create an ArtistCarousel composable
                // ArtistCarousel(title = "Similar Artists", artists = artistDetails!!.similarArtists)
            }
        }
    } else {
        Text("Could not load artist details.")
    }
}