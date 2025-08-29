package com.colux.libretune.ui.artist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.ui.components.artist.ArtistCarousel
import com.colux.libretune.ui.components.playlist.PlaylistCarousel
import com.colux.libretune.ui.components.playlist.PlaylistItem
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel

@Composable
fun ArtistScreen(
    artistId: String,
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
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


            item {
                Text(
                    text = "Top Songs",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                )
            }

            // The list of songs comes right after the title
            items(artistDetails!!.topSongs) { song ->
                SongItem(
                    song = song,
                    onClick = {
                        playerViewModel.playSongById(song.id)
                    }
                )
            }

            // Similar Artists Carousel
            if (artistDetails!!.albums.isNotEmpty()) {
                item {
                    PlaylistCarousel(
                        title = "Albums",
                        playlists = artistDetails!!.albums.map {
                            PlaylistItem(id = it.id, title = it.name, imageUrl = it.thumbnailUrl)
                        },
                        onItemClick = { index ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(artistDetails!!.albums[index].id))
                        }
                    )
                }
            }

            if (artistDetails!!.singlesAndEPs.isNotEmpty()) {
                item {
                    PlaylistCarousel(
                        title = "Singles & EPs",
                        playlists = artistDetails!!.singlesAndEPs.map {
                            PlaylistItem(id = it.id, title = it.name, imageUrl = it.thumbnailUrl)
                        },
                        onItemClick = { index ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(artistDetails!!.singlesAndEPs[index].id))
                        }
                    )
                }
            }

            if (artistDetails!!.playlists.isNotEmpty()) {
                item {
                    PlaylistCarousel(
                        title = "Playlists by ${artistDetails!!.name}",
                        playlists = artistDetails!!.playlists.map {
                            PlaylistItem(id = it.id, title = it.name, imageUrl = it.thumbnailUrl)
                        },
                        onItemClick = { index ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(artistDetails!!.playlists[index].id))
                        }
                    )
                }
            }

            if (artistDetails!!.featuring.isNotEmpty()) {
                item {
                    PlaylistCarousel(
                        title = "Featuring ${artistDetails!!.name}",
                        playlists = artistDetails!!.featuring.map {
                            PlaylistItem(id = it.id, title = it.name, imageUrl = it.thumbnailUrl)
                        },
                        onItemClick = { index ->
                            navController.navigate(Screen.PlaylistDetail.createRoute(artistDetails!!.featuring[index].id))
                        }
                    )
                }
            }

            if (artistDetails!!.similarArtists.isNotEmpty()) {
                item {
                    ArtistCarousel(
                        title = "Similar Artists",
                        artists = artistDetails!!.similarArtists,
                        onItemClick = { artistId ->
                            navController.navigate(Screen.Artist.createRoute(artistId))
                        }
                    )
                }
            }


            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    } else {
        Text("Could not load artist details.")
    }
}