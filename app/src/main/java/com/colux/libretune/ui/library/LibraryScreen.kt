package com.colux.libretune.ui.library

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.colux.libretune.ui.components.song.SongItem
import com.colux.libretune.ui.player.PlayerViewModel

@Composable
fun LibraryScreen(playerViewModel: PlayerViewModel) {
    val viewModel: LibraryViewModel = hiltViewModel()
    val likedSongs by viewModel.likedSongs.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "Liked Songs",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        items(likedSongs) { song ->
            // You can reuse your SearchResultItem or create a new one
            SongItem(song = song, onClick = { playerViewModel.playSongById(song.id) })
        }


        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
