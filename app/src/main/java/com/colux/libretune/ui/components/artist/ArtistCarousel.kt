package com.colux.libretune.ui.components.artist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.colux.libretune.R
import com.colux.libretune.data.model.Artist


@Composable
fun ArtistCarousel(
    title: String,
    artists: List<Artist>,
    onItemClick: (String) -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(artists) { artist ->
                Column(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .width(120.dp)
                        .clickable(onClick = { onItemClick(artist.id) }),
                    horizontalAlignment = Alignment.CenterHorizontally,

                    ) {
                    AsyncImage(
                        model = artist.bestImageForCarousel(),
                        contentDescription = artist.name,
                        placeholder = painterResource(id = R.drawable.ic_default_artist_avatar_foreground),
                        error = painterResource(id = R.drawable.ic_default_artist_avatar_foreground),
                        modifier = Modifier
                            .fillMaxWidth()        // Fills the 120.dp width of the parent
                            .aspectRatio(1f)     // Makes the height equal to the width (a square)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = artist.name,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}