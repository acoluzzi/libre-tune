package com.colux.libretune.ui.components.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.colux.libretune.R
import com.colux.libretune.data.model.Playlist

@Composable
fun PlaylistCarousel(
    title: String? = null,
    playlists: List<Playlist>,
    onItemClick: (itemIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    onViewAllClick: (() -> Unit)? = null,
    relatedPlaylist: Playlist? = null,
    onRelatedPlaylistClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
    ) {
        if (relatedPlaylist != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRelatedPlaylistClick(relatedPlaylist.id) }
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = relatedPlaylist.bestImageUrlForCarousel(),
                    contentDescription = relatedPlaylist.name,
                    placeholder = painterResource(id = R.drawable.ic_default_playlist_foreground),
                    error = painterResource(id = R.drawable.ic_default_playlist_foreground),
                    modifier = Modifier
                        .size(40.dp) // A good size for a header icon
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Similar Releases of",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Text(
                        text = relatedPlaylist.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.basicMarquee()
                    )
                }


            }
        } else {
            // Original header, shown only if relatedToArtist is null and a title is provided
            if (title?.isNotBlank() == true) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 8.dp
                    )
                )
            }
        }
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