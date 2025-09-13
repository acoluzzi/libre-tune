package com.colux.libretune.ui.add_to_playlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.colux.libretune.ui.library.PlaylistImageCollage
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistScreen(
    songId: String,
    navController: NavController,
    playerViewModel: PlayerViewModel,
    viewModel: AddToPlaylistViewModel = hiltViewModel()
) {
    val playlists by viewModel.playlists.collectAsState()
    val song by viewModel.song.collectAsState(initial = null)
    // Collect the new state for the button
    val isSongInAnyPlaylist by viewModel.isSongInAnyPlaylist.collectAsState()

    val currentlyPlayingSong by playerViewModel.currentSong.collectAsState()

    // 2. Calculate the necessary bottom padding. If a song is playing, add space.
    val bottomPadding = if (currentlyPlayingSong != null) 80.dp else 16.dp

    Scaffold(
        topBar = {
            // Use a custom header instead of the default TopAppBar
            AddToPlaylistHeader(
                isRemoveEnabled = isSongInAnyPlaylist,
                onRemoveClick = { viewModel.removeSongFromAllPlaylists() },
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(playlists) { playlistSelection ->
                    PlaylistSelectionItem(
                        playlistWithSongs = playlistSelection,
                        onCheckedChange = {
                            viewModel.toggleSongInPlaylist(
                                playlistSelection.playlist.playlist?.id
                                    ?: return@PlaylistSelectionItem,
                                playlistSelection.containsSong
                            )
                        }
                    )
                }

                item {
                    // This outer Row is used just for padding and positioning within the list
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Use a Surface for a customizable, clickable "chip"
                        Surface(
                            onClick = { navController.navigate(Screen.CreatePlaylist.route) },
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New playlist",
                                    modifier = Modifier.size(24.dp), // Reduced size for a chip
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Create new playlist",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = bottomPadding, top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Done", modifier = Modifier.padding(8.dp))
            }

        }

    }
}

/**
 * A custom header for the AddToPlaylistScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToPlaylistHeader(
    isRemoveEnabled: Boolean,
    onRemoveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // The first line is the standard top app bar
        TopAppBar(
            title = {
                Text(
                    text = "Add Song to Playlist",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        // The second line is the "Remove from all" button, aligned to the end
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onRemoveClick,
                enabled = isRemoveEnabled // Button is enabled based on the ViewModel state
            ) {
                Text("Remove from all")
            }
        }
    }
}


@Composable
fun PlaylistSelectionItem(
    playlistWithSongs: PlaylistForSelection,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaylistImageCollage(
            imageUrls = playlistWithSongs.playlist.songs.mapNotNull { it.getBestImageUrl() },
            size = 64.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlistWithSongs.playlist.playlist?.name ?: "Unknown Playlist",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${playlistWithSongs.playlist.songs.size} songs",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Checkbox(
            checked = playlistWithSongs.containsSong,
            onCheckedChange = { onCheckedChange() }
        )
    }
}