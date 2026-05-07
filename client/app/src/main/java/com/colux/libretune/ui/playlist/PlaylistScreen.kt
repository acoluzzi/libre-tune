package com.colux.libretune.ui.playlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.R
import com.colux.libretune.data.model.PlaylistDetails
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.components.playlist.PlaylistCarousel
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.components.song.SongMenu
import com.colux.libretune.ui.library.PlaylistImageCollage
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel
import com.colux.libretune.ui.search.SongItemSkeleton


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistId: String,
    playerViewModel: PlayerViewModel,
    navController: NavHostController,
    viewModel: PlaylistDetailViewModel = koinViewModel()
) {


    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberLazyListState()
    val showTopBar by remember {
        derivedStateOf { scrollState.firstVisibleItemIndex > 0 }
    }
    val sheetState = rememberModalBottomSheetState()
    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }


    val currentlyPlayedPlaylist by playerViewModel.currentPlaylistId
        .collectAsState(initial = null)

    val isPlaying by playerViewModel.isPlaying.collectAsState(
        initial = false
    )

    val isPlayingThisPlaylist = isPlaying && currentlyPlayedPlaylist == playlistId

    val isSaved by viewModel.isPlaylistSaved.collectAsState(
        initial = false
    )

    when (val state = uiState) {
        is PlaylistUiState.Loading -> PlaylistDetailSkeleton()
        is PlaylistUiState.Success -> {
            val playlistDetails = state.details
            Scaffold(
                    topBar = {
                        // The top bar is only visible when showTopBar is true
                        AnimatedVisibility(
                            visible = showTopBar,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CollapsingTopAppBar(
                                isPlayEnabled = playlistDetails.songs.isNotEmpty(),
                                playlistName = playlistDetails.name,
                                onBackClick = { navController.popBackStack() },
                                onPlayClick = {
                                    if (currentlyPlayedPlaylist == playlistDetails.id)
                                        playerViewModel.onPlayPauseClick()
                                    else {
                                        playerViewModel.playPlaylist(playlistDetails)
                                    }
                                },
                                isPlaying = isPlayingThisPlaylist
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = scrollState
                        ) {
                            // Item 0: The main header
                            item {
                                PlaylistHeader(
                                    details = playlistDetails,
                                    onPlayPauseClick = {
                                        if (currentlyPlayedPlaylist == playlistDetails.id)
                                            playerViewModel.onPlayPauseClick()
                                        else {
                                            playerViewModel.playPlaylist(playlistDetails)
                                        }
                                    },
                                    isPlaying = isPlayingThisPlaylist,
                                    onShuffleClick = {
                                        playerViewModel.shufflePlayPlaylist(playlistDetails)
                                    },
                                    isSaved = isSaved,
                                    isLocal = playlistDetails.isLocal,
                                    onLike = {
                                        viewModel.likePlaylist()
                                    },
                                    onDislike = {
                                        viewModel.dislikePlaylist()
                                    }
                                )
                            }


                            if (playlistDetails.songs.isNotEmpty()) {
                                // The list of songs
                                itemsIndexed(playlistDetails.songs) { index, song ->
                                    SongItem(
                                        song = song,
                                        playerViewModel = playerViewModel,
                                        onClick = {
                                            playerViewModel.playPlaylist(
                                                playlistDetails,
                                                index
                                            )
                                        },
                                        onMoreClick = {
                                            selectedSongForMenu = song
                                        },
                                        displayingInPlaylistId = playlistDetails.id,
                                        displayingInLocalPlaylist = playlistDetails.isLocal,
                                        navController = navController,
                                    )
                                }
                            } else {
                                if (playlistDetails.isLocal) {
                                    item {
                                        Text(
                                            "This playlist is empty. Add songs from your library or online sources.",
                                            modifier = Modifier.padding(16.dp)
                                        )
                                    }
                                } else {
                                    item {
                                        repeat(10) {
                                            SongItemSkeleton()
                                        }
                                    }
                                }
                            }

                            if (playlistDetails.relatedPlaylists.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

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


                            // Spacer for bottom player
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }

                        if (!showTopBar) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
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

        is PlaylistUiState.Error -> Text("Could not load playlist details.")
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
                playlistId = uiState.let {
                    if (it is PlaylistUiState.Success) it.details?.id else null
                }
            )
        }
    }
}


@Composable
fun PlaylistHeader(
    details: PlaylistDetails,
    isPlaying: Boolean,
    isLocal: Boolean,
    isSaved: Boolean,
    onPlayPauseClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit
) {


    Column(modifier = Modifier.padding(16.dp)) {
        // --- Image and Metadata Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val showCollage = details.isLocal && details.songs.isNotEmpty()

            if (showCollage) {
                val imageUrls = remember(details.id) {
                    details.songs.shuffled().take(4)
                        .mapNotNull { it.getBestImageUrl() }
                }
                PlaylistImageCollage(
                    imageUrls = imageUrls,
                    size = 150.dp
                )
            } else {
                val useDefaultImage =
                    details.isLocal && details.songs.isEmpty()
                val imageModel = if (useDefaultImage) {
                    R.drawable.default_playlist_image
                } else {
                    details.bestImage() // The remote URL
                }

                AsyncImage(
                    model = imageModel,
                    contentDescription = "Playlist Art",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }




            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    details.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.basicMarquee(),
                    maxLines = 2
                )

                if (details.artists.isNotEmpty()) {
                    Text(
                        details.getArtistNames(), style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.basicMarquee(),
                    )
                }

                Text(
                    text = "${details.songs.size} songs" +
                            if (details.totalDurationSeconds > 0)
                                " • ${details.getFormattedTotalDuration()}"
                            else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (details.releaseYear > 0) {
                    Text(
                        "Released in ${details.releaseYear}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Buttons Row ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isLocal) {
                IconButton(onClick = {
                    if (isSaved) onDislike() else onLike()
                }) {
                    Icon(
                        if (isSaved) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        contentDescription = "Like Playlist"
                    )
                }
            }

            IconButton(onClick = { /* TODO: Show menu */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More options")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Right-aligned buttons
            IconButton(
                onClick = {
                    onShuffleClick()
                },
                enabled = details.songs.isNotEmpty()
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
                    onPlayPauseClick()
                },
                modifier = Modifier.size(56.dp),
                enabled = details.songs.isNotEmpty()
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    modifier = Modifier.size(32.dp)
                )
            }
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