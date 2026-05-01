package com.colux.libretune.ui.mood_genre

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.artist.ArtistCarouselSkeleton
import com.colux.libretune.ui.components.playlist.PlaylistCarousel
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.search.SongItemSkeleton
import com.colux.libretune.ui.search.TitleSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodGenreScreen(
    moodGenreId: String,
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    viewModel: MoodGenreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()

    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState is GenreMoodUiState.Success) (uiState as GenreMoodUiState.Success).details.name else "Mood & Genre") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = scrollState,

                ) {
                when (uiState) {
                    is GenreMoodUiState.Loading -> {
                        item {
                            MoodGenreScreenSkeleton()
                        }
                    }

                    is GenreMoodUiState.Success -> {
                        val details = (uiState as GenreMoodUiState.Success).details


                        if (details.songs.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Songs",
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // The list of songs comes right after the title
                            items(details.songs.take(10)) { song ->
                                SongItem(
                                    song = song,
                                    playerViewModel = playerViewModel,
                                    onClick = {
                                        playerViewModel.playSongList(
                                            details.songs,
                                            details.songs.indexOf(song)
                                        )
                                    },
                                    onMoreClick = {
                                        selectedSongForMenu = song
                                    },
                                    navController = navController,
                                )
                            }


                        }


                        if (details.carousels.isNotEmpty()) {
                            details.carousels.forEach { carousel ->
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                item {
                                    PlaylistCarousel(
                                        title = carousel.title,
                                        playlists = carousel.playlists,
                                        onItemClick = {
                                            navController.navigate(
                                                Screen.PlaylistDetail.createRoute(
                                                    carousel.playlists[it].id
                                                )
                                            )
                                        }
                                    )
                                }

                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }


                    }

                    is GenreMoodUiState.Error -> {
                        val message = (uiState as GenreMoodUiState.Error).message
                        // Show error message
                    }
                }
            }
        }


    }

}


@Composable
fun MoodGenreScreenSkeleton() {
    Column {
        TitleSkeleton()

        // 3. List of Song Skeletons
        SongItemSkeleton()

        // 4. Spacer
        Spacer(modifier = Modifier.height(16.dp))

        // 5. Carousel Skeleton (for Albums or Similar Artists)
        ArtistCarouselSkeleton()

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Carousel Skeleton (for Albums or Similar Artists)
        ArtistCarouselSkeleton()

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Carousel Skeleton (for Albums or Similar Artists)
        ArtistCarouselSkeleton()

        Spacer(modifier = Modifier.height(64.dp))
    }

}
