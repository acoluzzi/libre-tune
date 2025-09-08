package com.colux.libretune.ui.components.song

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.colux.libretune.data.model.Song
import com.colux.libretune.ui.nav.Screen
import com.colux.libretune.ui.player.PlayerViewModel

@Composable
fun SongItem(
    song: Song, onClick: () -> Unit,
    playerViewModel: PlayerViewModel,
    onMoreClick: () -> Unit,
    isInPlaylist: Boolean,
    navController: NavHostController
) {

    val selectedSong by playerViewModel.currentSong.collectAsState()
    val selectedSongIsPlaying by playerViewModel.isPlaying.collectAsState()
    val savedSongIds by playerViewModel.savedSongIds.collectAsState()

    val isSelectedSong = song.id == selectedSong?.id
    val isSaved = savedSongIds.contains(song.id)
    val itemShape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(itemShape)
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onMoreClick
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(56.dp)) {
            AsyncImage(
                model = song.getBestImageUrl(),
                contentDescription = "Album art for ${song.title}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Conditionally display the animating bars
                if (isSelectedSong) {
                    // Pass the playing state to the indicator
                    PlayingIndicator(isPlaying = selectedSongIsPlaying)
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                    color = if (isSelectedSong) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = song.getArtistNames(),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.basicMarquee(),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        if (isSaved && !isInPlaylist) {
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = {
                navController.navigate(Screen.AddToPlaylist.createRoute(song.id))
            }, modifier = Modifier.width(24.dp)) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "More options for ${song.title}"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        IconButton(onClick = onMoreClick, modifier = Modifier.width(24.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options for ${song.title}"
            )
        }
    }
}


@Composable
fun PlayingIndicator(
    // The component now knows if the song is playing or paused
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier
            .width(16.dp)
            .height(16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (isPlaying) {
            // If playing, run the infinite animation
            val infiniteTransition =
                rememberInfiniteTransition(label = "playing_indicator_transition")
            val animatedScales = (1..3).map { index ->
                infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 400 + (index * 100),
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_height_$index"
                )
            }
            animatedScales.forEach { scale ->
                Bar(scale = scale.value, color = color)
            }
        } else {
            // If paused, show static bars
            val staticScales = remember { listOf(0.4f, 0.7f, 0.5f) }
            staticScales.forEach { scale ->
                Bar(scale = scale, color = color)
            }
        }
    }
}

// Helper composable for a single bar to avoid code repetition
@Composable
private fun RowScope.Bar(scale: Float, color: Color) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight(scale)
            .background(color = color, shape = RoundedCornerShape(1.dp))
    )
}