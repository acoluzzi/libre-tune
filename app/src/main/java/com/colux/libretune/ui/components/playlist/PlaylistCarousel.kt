package com.colux.libretune.ui.components.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.colux.libretune.data.model.Playlist

@Composable
fun PlaylistCarousel(
    title: String,
    playlists: List<Playlist>,
    onItemClick: (itemIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    onViewAllClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
    ) {
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
            itemsIndexed(playlists) { index, playlist ->
                if (playlist.id.isEmpty() || playlist.name.isEmpty() || playlist.images.isEmpty()) {
                    return@itemsIndexed
                }
                PlaylistCard(playlist = playlist, onSongClick = { onItemClick(index) })
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
    // The card maintains the same size to fit in the carousel.
    Card(
        modifier = Modifier
            .size(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        // A Column is used to center the content.
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // A Box creates the circular background for the icon.
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View All",
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "View All",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}