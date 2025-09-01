package com.colux.libretune.ui.components.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Playlist

@Composable
fun PlaylistItem(album: Playlist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = album.images.firstOrNull()?.url,
            contentDescription = album.name,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = album.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
            Text(
                text = album.getArtistNames(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}