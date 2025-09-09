package com.colux.libretune.ui.artist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.components.artist.ArtistCarousel
import com.colux.libretune.ui.components.playlist.PlaylistCarousel
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.components.song.SongMenu
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.search.SongItemSkeleton
import com.colux.libretune.ui.search.TitleSkeleton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    artistId: String,
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    viewModel: ArtistViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }
    val scrollState = rememberLazyListState()
    val showTopBar by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
    }

    val isSaved by viewModel.isArtistSaved.collectAsState(
        initial = false
    )

    val currentlyPlayedArtistIds by playerViewModel.currentArtistIds.collectAsState(initial = emptyList())

    val isPlaying by playerViewModel.isPlaying.collectAsState(
        initial = false
    )

    val isArtistSelectedInPlayer = currentlyPlayedArtistIds.contains(artistId)


    val isPlayingThisArtist = isPlaying && isArtistSelectedInPlayer

    when (val state = uiState) {
        is ArtistUiState.Loading -> ArtistScreenSkeleton()

        is ArtistUiState.Success -> {

            Scaffold(
                topBar = {
                    AnimatedVisibility(
                        visible = showTopBar,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        CollapsingTopAppBar(
                            playlistName = state.details.name,
                            isPlayEnabled = state.details.topSongs.isNotEmpty(),
                            isPlaying = isPlayingThisArtist,
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onPlayClick = {
                                if (isArtistSelectedInPlayer)
                                    playerViewModel.onPlayPauseClick()
                                else {
                                    playerViewModel.playArtist(state.details)
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->


                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {


                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        state = scrollState,
                        contentPadding = PaddingValues(
                            bottom = innerPadding.calculateBottomPadding(),
                        )

                    ) {
                        // Artist Banner
                        item {
                            Box(modifier = Modifier.height(260.dp)) {
                                AsyncImage(
                                    model = state.details.getImageUrlForBanner(),
                                    contentDescription = "Artist Banner",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.9f)
                                                )
                                            )
                                        )
                                )
                                Text(
                                    text = state.details.name,
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    if (isSaved) {
                                        viewModel.dislikeArtist()
                                    } else {
                                        viewModel.likeArtist()
                                    }
                                }
                                ) {
                                    Icon(
                                        if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        contentDescription = "Like Artist"
                                    )
                                }


                                IconButton(onClick = { /* TODO: Show menu */ }) {
                                    Icon(
                                        Icons.Default.MoreVert,
                                        contentDescription = "More options"
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Right-aligned buttons
                                IconButton(
                                    onClick = {
                                        playerViewModel.shufflePlayArtist(state.details)
                                    },
                                    enabled = true
                                ) {
                                    Icon(
                                        Icons.Default.Shuffle,
                                        contentDescription = "Shuffle",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                FilledIconButton(
                                    onClick = {
                                        if (isArtistSelectedInPlayer)
                                            playerViewModel.onPlayPauseClick()
                                        else {
                                            playerViewModel.playArtist(state.details)
                                        }
                                    },
                                    modifier = Modifier.size(56.dp),
                                    enabled = state.details.topSongs.isNotEmpty()
                                ) {
                                    Icon(
                                        if (isPlayingThisArtist) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Play",
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
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
                                    modifier = Modifier.padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = 16.dp
                                    )
                                )
                            }

                            // The list of songs comes right after the title
                            items(state.details.topSongs) { song ->
                                SongItem(
                                    song = song,
                                    playerViewModel = playerViewModel,
                                    onClick = {
                                        playerViewModel.playSongList(
                                            state.details.topSongs,
                                            state.details.topSongs.indexOf(song)
                                        )
                                    },
                                    onMoreClick = {
                                        selectedSongForMenu = song
                                    },
                                    navController = navController,
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        val allReleases = (state.details.albums + state.details.singlesAndEPs)
                            .sortedByDescending { it.releaseYear }
                            .take(10)

                        if (allReleases.isNotEmpty()) {
                            item {
                                PlaylistCarousel(
                                    title = "Releases",
                                    playlists = allReleases,
                                    onItemClick = { index ->
                                        navController.navigate(
                                            Screen.PlaylistDetail.createRoute(
                                                allReleases[index].id
                                            )
                                        )
                                    },
                                    onViewAllClick = if (allReleases.size >= 10) {
                                        {
                                            navController.navigate(
                                                Screen.Discography.createRoute(artistId)
                                            )
                                        }
                                    } else {
                                        null
                                    }


                                )
                            }
                        }


                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (state.details.featuring.isNotEmpty()) {
                            item {
                                PlaylistCarousel(
                                    title = "Featuring ${state.details.name}",
                                    playlists = state.details.featuring,
                                    modifier = Modifier.height(240.dp),
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

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        if (state.details.playlists.isNotEmpty()) {
                            item {
                                PlaylistCarousel(
                                    title = "Artist Selections",
                                    playlists = state.details.playlists,
                                    modifier = Modifier.height(240.dp),
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

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
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
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    if (!showTopBar) {
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(innerPadding)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }

                }

            }


        }

        is ArtistUiState.Error -> Text("Could not load artist details: ${state.message}")
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


/**
 * The TopAppBar that appears when the user scrolls down.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingTopAppBar(
    playlistName: String,
    isPlayEnabled: Boolean,
    isPlaying: Boolean,
    onBackClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    TopAppBar(
        title = { Text(playlistName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            FilledIconButton(
                onClick = onPlayClick,
                modifier = Modifier.size(40.dp),
                enabled = isPlayEnabled
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}