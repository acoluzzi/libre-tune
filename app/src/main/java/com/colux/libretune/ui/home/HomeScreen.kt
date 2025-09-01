package com.colux.libretune.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.components.playlist.PlaylistCarousel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSongClick: (playlist: List<Song>, songIndex: Int) -> Unit
) {
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()
    val madeForYou by viewModel.madeForYou.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp) // Space between carousels
    ) {
        item {
            // A nice greeting
            Text(
                text = "Good afternoon", // You can make this dynamic based on time!
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        item {
            PlaylistCarousel(
                title = "Recently Played",
                playlists = listOf(),
                onItemClick = { index ->
                    onSongClick(recentlyPlayed, index)
                })
        }

        item {
            PlaylistCarousel(
                title = "Made for You",
                playlists = listOf(),
                onItemClick = { index ->
                    onSongClick(madeForYou, index)
                })
        }



        item {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}