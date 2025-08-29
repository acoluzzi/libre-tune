package com.colux.libretune.ui.components.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlaylistCarousel(
    title: String,
    playlists: List<PlaylistItem>,
    onItemClick: (itemIndex: Int) -> Unit,
    onViewAllClick: (() -> Unit)? = null
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(MaterialTheme.colorScheme.surface)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            itemsIndexed(playlists) { index, song ->
                PlaylistCard(playlist = song, onSongClick = { onItemClick(index) })
            }

            if (onViewAllClick != null) {
                item {
                    ViewAllCard(onClick = onViewAllClick)
                }
            }
        }
    }
}

@Composable
fun ViewAllCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(160.dp) // Match the size of your other items
            .padding(end = 16.dp)
            .clickable(onClick = onClick),
        // ... styling
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("View All")
        }
    }
}