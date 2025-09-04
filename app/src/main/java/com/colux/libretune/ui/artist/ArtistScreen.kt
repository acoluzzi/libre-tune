package com.colux.libretune.ui.artist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.search.SongItemSkeleton
import com.colux.libretune.ui.search.TitleSkeleton

@Composable
fun ArtistScreen(
    artistId: String,
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()


    when (val state = uiState) {
        is ArtistUiState.Loading -> {
            ArtistScreenSkeleton()
        }

        is ArtistUiState.Success -> {
            // Your existing LazyColumn UI goes here.
            // You can access the data via 'state.details'
            LazyColumn(modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()) {
                // Artist Banner
                item {
                    Box(modifier = Modifier.height(200.dp)) {
                        AsyncImage(
                            model = state.details.getImageUrlForBanner(),
                            contentDescription = "Artist Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = state.details.name,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        )
                    }
                }




                if (state.details.topSongs.isEmpty()) {
                    // 2. "Top Songs" Title Skeleton
                    item {
                        TitleSkeleton()
                    }

                    // 3. List of Song Skeletons
                    items(5) {
                        SongItemSkeleton()
                    }

                    // 4. Spacer
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 5. Carousel Skeleton (for Albums or Similar Artists)
                    item {
                        ArtistCarouselSkeleton()
                    }

                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                } else {
                    item {
                        Text(
                            text = "Top Songs",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        )
                    }

                    // The list of songs comes right after the title
                    items(state.details.topSongs) { song ->
                        SongItem(
                            song = song,
                            onClick = {
                                playerViewModel.playPlaylist(
                                    state.details.topSongs,
                                    state.details.topSongs.indexOf(song)
                                )
                            }
                        )
                    }

                    if (state.details.topSongs.size == 5 && state.details?.topSongPlaylist != null) {
                        item {
                            TextButton(
                                onClick = {
                                    // Navigate to the PlaylistDetailScreen using the uploads ID
                                    state.details?.topSongPlaylist.let { playlist ->
                                        if (playlist != null) {
                                            navController.navigate(
                                                Screen.PlaylistDetail.createRoute(
                                                    playlist.id
                                                )
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("More")
                            }
                        }
                    }
                }


                // Similar Artists Carousel
                if (state.details!!.albums.isNotEmpty()) {
                    item {
                        PlaylistCarousel(
                            title = "Albums", modifier = Modifier.height(220.dp),
                            playlists = state.details.albums,
                            onItemClick = { index ->
                                navController.navigate(
                                    Screen.PlaylistDetail.createRoute(
                                        state.details.albums[index].id
                                    )
                                )
                            },

                            )
                    }
                }

                if (state.details.singlesAndEPs.isNotEmpty()) {
                    item {
                        PlaylistCarousel(
                            title = "Singles & EPs", modifier = Modifier.height(220.dp),
                            playlists = state.details.singlesAndEPs,
                            onItemClick = { index ->
                                navController.navigate(
                                    Screen.PlaylistDetail.createRoute(
                                        state.details.singlesAndEPs[index].id
                                    )
                                )
                            },
                        )
                    }
                }

                if (state.details.playlists.isNotEmpty()) {
                    item {
                        PlaylistCarousel(
                            title = "Playlists by ${state.details.name}",
                            modifier = Modifier.height(220.dp),
                            playlists = state.details.playlists,
                            onItemClick = { index ->
                                navController.navigate(
                                    Screen.PlaylistDetail.createRoute(
                                        state.details.playlists[index].id
                                    )
                                )
                            }
                        )
                    }
                }

                if (state.details.featuring.isNotEmpty()) {
                    item {
                        PlaylistCarousel(
                            title = "Featuring ${state.details.name}",
                            playlists = state.details.featuring, modifier = Modifier.height(220.dp),
                            onItemClick = { index ->
                                navController.navigate(
                                    Screen.PlaylistDetail.createRoute(
                                        state.details.featuring[index].id
                                    )
                                )
                            }
                        )
                    }
                }

                if (state.details.similarArtists.isNotEmpty()) {
                    item {
                        ArtistCarousel(
                            title = "Similar Artists",
                            artists = state.details.similarArtists,
                            modifier = Modifier.height(200.dp),
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

        }

        is ArtistUiState.Error -> {
            Text("Could not load artist details: ${state.message}")
        }
    }
    // TODO handle items continuation

}