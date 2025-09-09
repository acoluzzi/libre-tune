package com.colux.libretune.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.R
import com.colux.libretune.data.model.wrapper.PlaylistWithSongs
import com.colux.libretune.ui.nav.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavHostController) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val playlists by viewModel.playlists.collectAsState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Library") },
                actions = {
                    // "Plus" button to show the create playlist dialog
                    IconButton(onClick = { navController.navigate(Screen.CreatePlaylist.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Create new playlist")
                    }
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 80.dp) // Space for mini-player
        ) {
            items(playlists) { playlist ->
                PlaylistListItem(
                    playlistWithSongs = playlist,
                    onClick = {
                        navController.navigate(
                            Screen.PlaylistDetail.createRoute(playlist.playlist?.id ?: "")
                        )
                    }
                )
            }
        }
    }
}

/**
 * A composable for displaying a single playlist in the library list.
 */
@Composable
fun PlaylistListItem(
    playlistWithSongs: PlaylistWithSongs,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (playlistWithSongs.hasToShowCollageInLibrary()) {
            val imageUrls = remember(playlistWithSongs.playlist?.id) {
                playlistWithSongs.getImages()
            }

            PlaylistImageCollage(
                imageUrls = imageUrls,
                size = 64.dp, // Match your desired item size
                modifier = Modifier.size(64.dp) // Maintain consistent size for the row
            )
        } else {
            AsyncImage(
                model = playlistWithSongs.bestImage() ?: R.mipmap.ic_default_liked_songs,
                contentDescription = "Playlist Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clipToBounds()
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        Column {
            playlistWithSongs.playlist?.name?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Text(
                text = playlistWithSongs.getSubtitle(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

