package com.colux.libretune.ui.components.playlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Playlist

@Composable
fun PlaylistCard(playlist: Playlist, onSongClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(160.dp) // Set a fixed width for the card
            .padding(end = 16.dp)
            .clickable(onClick = onSongClick)
    ) {
        AsyncImage(
            model = playlist.bestImageUrlForCarousel(),
            contentDescription = "Album cover for ${playlist.name}",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f) // Makes the image square
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = playlist.name,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}