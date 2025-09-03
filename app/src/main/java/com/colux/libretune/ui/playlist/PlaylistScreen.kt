package com.colux.libretune.ui.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.ui.components.playlist.PlaylistCarousel
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.search.SongItemSkeleton

@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberLazyListState()

    val imageHeight = 300.dp
    val imageHeightPx = with(LocalDensity.current) { imageHeight.toPx() }



    when (val state = uiState) {
        is PlaylistUiState.Loading -> {
            PlaylistDetailSkeleton()
        }

        is PlaylistUiState.Success -> {


            Box(modifier = Modifier.fillMaxSize()) {
                // We only show the content when details are loaded
                state.details.let { playlistDetails ->
                    // --- Background Image with Parallax Effect ---
                    AsyncImage(
                        model = playlistDetails.bestImage(),
                        contentDescription = "Playlist Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(imageHeight)
                            // 1. Clip the image so it doesn't draw outside its bounds when moved
                            .clipToBounds()
                            .graphicsLayer {
                                // 2. Move the image up at half the scroll speed
                                translationY = 0.5f * scrollState.firstVisibleItemScrollOffset

                                // 3. Fade the image out as it scrolls
                                alpha =
                                    1f - (scrollState.firstVisibleItemScrollOffset / imageHeightPx)
                            }
                    )

                    // --- Scrollable Song List ---
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = scrollState
                    ) {
                        // 1. A transparent spacer that pushes the song list below the image
                        item {
                            Spacer(modifier = Modifier.height(300.dp))
                        }

                        // 2. Playlist Title and Artist
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    playlistDetails.name,
                                    style = MaterialTheme.typography.headlineLarge
                                )

                                if (playlistDetails.getArtistNames().isNotEmpty()) {
                                    Text(
                                        playlistDetails.getArtistNames(),
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                }
                            }

                        }

                        if (playlistDetails.songs.isNotEmpty()) {
                            // 3. The list of songs
                            itemsIndexed(playlistDetails.songs) { index, song ->
                                SongItem(
                                    song = song,
                                    onClick = {
                                        playerViewModel.playPlaylist(
                                            playlistDetails.songs,
                                            index
                                        )
                                    }
                                )
                            }
                        } else {
                            item {
                                repeat(10) {
                                    SongItemSkeleton()
                                }
                            }
                        }



                        if (playlistDetails.relatedPlaylists.isNotEmpty()) {
                            item {
                                PlaylistCarousel(
                                    title = "You may Also Like",
                                    playlists = playlistDetails.relatedPlaylists,
                                    onItemClick = { index ->
                                        navController.navigate(
                                            Screen.PlaylistDetail.createRoute(
                                                playlistDetails.relatedPlaylists[index].id
                                            )
                                        )
                                    }
                                )
                            }
                        }


                        item {
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }
            }
        }

        is PlaylistUiState.Error -> {
            Text("Could not load artist details: ${state.message}")
        }
    }


}